package net.caffeinemc.mods.lithium.common.world.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class ChunkStatusTracker {

    //Add other callback types in the future when needed
    private static final ArrayList<BiConsumer<ServerLevel, ChunkPos>> UNLOAD_CALLBACKS = new ArrayList<>();
    private static final ArrayList<BiConsumer<ServerLevel, LevelChunk>> LOAD_CALLBACKS = new ArrayList<>();

    /**
     * Callback will be invoked a bit before the chunk is set to be accessible via the chunk map.
     *
     * @param callback Callback that receives the level and the chunk as argument.
     */
    public static void registerLoadCallback(BiConsumer<ServerLevel, LevelChunk> callback) {
        LOAD_CALLBACKS.add(callback);
    }

    /**
     * Callback will be invoked immediately after the chunk is set to be inaccessible via the chunk map.
     * @param callback Callback that receives the level and the chunk position as argument. The chunk itself is inaccessible now.
     */
    public static void registerUnloadCallback(BiConsumer<ServerLevel, ChunkPos> callback) {
        UNLOAD_CALLBACKS.add(callback);
    }

    public static void onChunkAccessible(ServerLevel serverLevel, LevelChunk levelChunk) {
        if (!serverLevel.getServer().isSameThread()) {
            throw new IllegalStateException("ChunkStatusTracker.onChunkAccessible called on wrong thread!");
        }

        for (int i = 0; i < LOAD_CALLBACKS.size(); i++) { //TODO confirm this is only called on the server thread...
            LOAD_CALLBACKS.get(i).accept(serverLevel, levelChunk);
        }
    }

    public static void onChunkInaccessible(ServerLevel serverLevel, ChunkPos pos) {
        if (!serverLevel.getServer().isSameThread()) {
            throw new IllegalStateException("ChunkStatusTracker.onChunkInaccessible called on wrong thread!");
        }

        for (int i = 0; i < UNLOAD_CALLBACKS.size(); i++) {
            UNLOAD_CALLBACKS.get(i).accept(serverLevel, pos);
        }
    }
}
