package net.caffeinemc.mods.lithium.mixin.util.chunk_status_tracking;

import net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin extends GenerationChunkHolder {

    public ChunkHolderMixin(ChunkPos chunkPos) {
        super(chunkPos);
    }

    @Shadow
    public abstract CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture();

    @Inject(
            method = "updateFutures(Lnet/minecraft/server/level/ChunkMap;Ljava/util/concurrent/Executor;)V", locals = LocalCapture.CAPTURE_FAILHARD,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/FullChunkStatus;isOrAfter(Lnet/minecraft/server/level/FullChunkStatus;)Z", ordinal = 6)
    )
    private void trackUpdate(ChunkMap chunkMap, Executor executor, CallbackInfo ci, FullChunkStatus prevStatus, FullChunkStatus status) {
        ServerLevel serverLevel = chunkMap.level;

        boolean loaded = status.isOrAfter(FullChunkStatus.FULL);
        boolean wasLoaded = prevStatus.isOrAfter(FullChunkStatus.FULL);
        if (!loaded && wasLoaded) {
            ChunkStatusTracker.onChunkInaccessible(serverLevel, this.pos);
        } else if (!wasLoaded) {
            //The chunk is loaded. Either the future still has work (-> the other mixin will handle it), or
            // the chunk is available immediately (-> we have to handle it here). This commonly happens at the edge of
            // the render distance when the player turns around and reloads a chunk that was only barely unloaded.
            ChunkAccess chunkAccess = this.getChunkIfPresentUnchecked(ChunkStatus.FULL);
            if (chunkAccess instanceof LevelChunk chunk) {
                ChunkStatusTracker.onChunkAccessible(serverLevel, chunk);
            }
        }

    }
}
