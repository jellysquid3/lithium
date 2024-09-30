package me.jellysquid.mods.lithium.mixin.world.chunk_access;

import me.jellysquid.mods.lithium.common.world.chunk.ChunkHolderExtended;
import net.minecraft.Util;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

/**
 * This patch makes a number of optimizations to chunk retrieval which helps to alleviate some of the slowdown introduced
 * in Minecraft 1.13+.
 * - Scanning the recent request cache is made faster through doing a single linear integer scan. This works through
 * encoding the request's position and status level into a single integer.
 * - Chunk tickets are only created during cache-misses if they were not already created this tick. This prevents the
 * creation of duplicate tickets which would only be immediately discarded after an expensive lookup and sort.
 * - Lambdas are replaced where possible to use simple if-else logic, avoiding allocations and variable captures.
 * - The chunk retrieval logic does not try to begin executing other tasks while blocked unless the future isn't
 * already complete.
 * <p>
 * There are also some organizational and differences which help the JVM to better optimize code here, most of which
 * are documented.
 */
@SuppressWarnings("OverwriteModifiers")
@Mixin(ServerChunkCache.class)
public abstract class ServerChunkManagerMixin {
    @Shadow
    @Final
    private ServerChunkCache.MainThreadExecutor mainThreadProcessor;

    @Shadow
    @Final
    private DistanceManager distanceManager;

    @Shadow
    @Final
    public ChunkMap chunkMap;

    @Shadow
    protected abstract ChunkHolder getVisibleChunkIfPresent(long pos);

    @Shadow
    @Final
    Thread mainThread;

    @Shadow
    protected abstract boolean chunkAbsent(ChunkHolder holder, int maxLevel);

    @Shadow
    public abstract void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks);

    @Shadow
    abstract boolean runDistanceManagerUpdates();

    private long time;

    @Inject(method = "tick", at = @At("HEAD"))
    private void preTick(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
        this.time++;
    }

    /**
     * @reason Optimize the function
     * @author JellySquid
     */
    @Overwrite
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean create) {
        if (Thread.currentThread() != this.mainThread) {
            return this.getChunkOffThread(x, z, status, create);
        }

        // Store a local reference to the cached keys array in order to prevent bounds checks later
        long[] cacheKeys = this.cacheKeys;

        // Create a key which will identify this request in the cache
        long key = createCacheKey(x, z, status);

        for (int i = 0; i < 4; ++i) {
            // Consolidate the scan into one comparison, allowing the JVM to better optimize the function
            // This is considerably faster than scanning two arrays side-by-side
            if (key == cacheKeys[i]) {
                ChunkAccess chunk = this.cacheChunks[i];

                // If the chunk exists for the key, or we didn't need to create one, return the result
                if (chunk != null || !create) {
                    return chunk;
                }
            }
        }

        // We couldn't find the chunk in the cache, so perform a blocking retrieval of the chunk from storage
        ChunkAccess chunk = this.getChunkBlocking(x, z, status, create);

        if (chunk != null) {
            this.addToCache(key, chunk);
        } else if (create) {
            throw new IllegalStateException("Chunk not there when requested");
        }

        return chunk;
    }

    private ChunkAccess getChunkOffThread(int x, int z, ChunkStatus status, boolean create) {
        return CompletableFuture.supplyAsync(() -> this.getChunk(x, z, status, create), this.mainThreadProcessor).join();
    }

    /**
     * Retrieves a chunk from the storages, blocking to work on other tasks if the requested chunk needs to be loaded
     * from disk or generated in real-time.
     *
     * @param x      The x-coordinate of the chunk
     * @param z      The z-coordinate of the chunk
     * @param leastStatus The minimum status level of the chunk
     * @param create True if the chunk should be loaded/generated if it isn't already, otherwise false
     * @return A chunk if it was already present or loaded/generated by the {@param create} flag
     */
    @Unique
    private ChunkAccess getChunkBlocking(int x, int z, ChunkStatus leastStatus, boolean create) {
        final long key = ChunkPos.asLong(x, z);
        final int level = ChunkLevel.byStatus(leastStatus);

        ChunkHolder holder = this.getVisibleChunkIfPresent(key);

        // Vanilla: Check if the holder is present and is at least of the level we need
        if (this.chunkAbsent(holder, level)) {
            if (create) {
                // Vanilla: The chunk holder is missing, so we need to create a ticket in order to load it
                this.createChunkLoadTicket(x, z, level);

                // Vanilla: Tick the chunk manager to have our new ticket processed
                this.runDistanceManagerUpdates();

                // Vanilla: Try to fetch the holder again now that we have requested a load
                holder = this.getVisibleChunkIfPresent(key);

                // Vanilla: If the holder is still not available, we need to fail now... something is wrong.
                if (this.chunkAbsent(holder, level)) {
                    throw Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            } else {
                //Vanilla: Use UNLOADED_FUTURE. Lithium: Just return null immediately.
                // The holder is absent, and we weren't asked to create anything, so return null
                return null;
            }
        } else if (create && ((ChunkHolderExtended) holder).lithium$updateLastAccessTime(this.time)) {
            // Vanilla: Always create the ticket.
            // Lithium: Only create a new chunk ticket if one hasn't already been submitted this tick
            // This maintains vanilla behavior (preventing chunks from being immediately unloaded) while also
            // eliminating the cost of submitting a ticket for most chunk fetches
            this.createChunkLoadTicket(x, z, level);
        }

        // Lithium: Attempt to directly get the chunk from the finished future:
        if (!((AbstractChunkHolderAccessor) holder).invokeCannotBeLoaded(leastStatus)) {
            CompletableFuture<ChunkResult<ChunkAccess>> directlyAccessedFuture = ((AbstractChunkHolderAccessor) holder).lithium$getChunkFuturesByStatus().get(leastStatus.getIndex());
            if (directlyAccessedFuture != null && directlyAccessedFuture.isDone()) {
                ChunkAccess chunk = directlyAccessedFuture.join().orElse(null);
                if (chunk != null) {
                    return chunk;
                }
            }
        }

        // Vanilla: Always call holder.load(). Lithium: Fall back to vanilla in case the fast-path did not work.
        CompletableFuture<ChunkResult<ChunkAccess>> loadFuture = holder.scheduleChunkGenerationTask(leastStatus, this.chunkMap);

        // Vanilla: Always call runTasks(). Lithium: Only call runTasks() if it will perform work.
        if (!loadFuture.isDone()) {
            // Perform other chunk tasks while waiting for this future to complete
            // This returns when either the future is done or there are no other tasks remaining
            this.mainThreadProcessor.managedBlock(loadFuture::isDone);
        }

        // Wait for the result of the future and unwrap it, returning null if the chunk is absent
        return loadFuture.join().orElse(null);

    }

    private void createChunkLoadTicket(int x, int z, int level) {
        ChunkPos chunkPos = new ChunkPos(x, z);

        this.distanceManager.addTicket(TicketType.UNKNOWN, chunkPos, level, chunkPos);
    }

    /**
     * The array of keys (encoding positions and status levels) for the recent lookup cache
     */
    private final long[] cacheKeys = new long[4];

    /**
     * The array of values associated with each key in the recent lookup cache.
     */
    private final ChunkAccess[] cacheChunks = new ChunkAccess[4];

    /**
     * Encodes a chunk position and status into a long. Uses 28 bits for each coordinate value, and 8 bits for the
     * status.
     */
    private static long createCacheKey(int chunkX, int chunkZ, ChunkStatus status) {
        return ((long) chunkX & 0xfffffffL) | (((long) chunkZ & 0xfffffffL) << 28) | ((long) status.getIndex() << 56);
    }

    /**
     * Prepends the chunk with the given key to the recent lookup cache
     */
    private void addToCache(long key, ChunkAccess chunk) {
        for (int i = 3; i > 0; --i) {
            this.cacheKeys[i] = this.cacheKeys[i - 1];
            this.cacheChunks[i] = this.cacheChunks[i - 1];
        }

        this.cacheKeys[0] = key;
        this.cacheChunks[0] = chunk;
    }

    /**
     * Reset our own caches whenever vanilla does the same
     */
    @Inject(method = "clearCache()V", at = @At("HEAD"))
    private void onCachesCleared(CallbackInfo ci) {
        Arrays.fill(this.cacheKeys, Long.MAX_VALUE);
        Arrays.fill(this.cacheChunks, null);
    }
}
