package net.caffeinemc.mods.lithium.common.tracking.block;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import net.caffeinemc.mods.lithium.common.block.BlockListeningSection;
import net.caffeinemc.mods.lithium.common.util.Pos;
import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.ArrayList;

public final class ChunkSectionChangeCallback {
    private ArrayList<SectionedBlockChangeTracker> trackers;

    public static void init() {
        if (BlockListeningSection.class.isAssignableFrom(LevelChunkSection.class)) {
            ChunkStatusTracker.registerUnloadCallback((serverWorld, chunkPos) -> {
                Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> changeCallbacks = ((LithiumData) serverWorld).lithium$getData().chunkSectionChangeCallbacks();
                int x = chunkPos.x;
                int z = chunkPos.z;
                for (int y = Pos.SectionYCoord.getMinYSection(serverWorld); y <= Pos.SectionYCoord.getMaxYSectionInclusive(serverWorld); y++) {
                    SectionPos chunkSectionPos = SectionPos.of(x, y, z);
                    ChunkSectionChangeCallback chunkSectionChangeCallback = changeCallbacks.remove(chunkSectionPos.asLong());
                    if (chunkSectionChangeCallback != null) {
                        chunkSectionChangeCallback.onChunkSectionInvalidated(chunkSectionPos);
                    }
                }
            });
        }
    }

    public ChunkSectionChangeCallback() {
    }

    public static ChunkSectionChangeCallback create(long sectionPos, Level world) {
        ChunkSectionChangeCallback chunkSectionChangeCallback = new ChunkSectionChangeCallback();
        Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> changeCallbacks = ((LithiumData) world).lithium$getData().chunkSectionChangeCallbacks();
        ChunkSectionChangeCallback previous = changeCallbacks.put(sectionPos, chunkSectionChangeCallback);
        if (previous != null) {
            previous.onChunkSectionInvalidated(SectionPos.of(sectionPos));
        }
        return chunkSectionChangeCallback;
    }

    public void onBlockChange(BlockListeningSection section) {
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers;
        this.trackers = null;
        if (sectionedBlockChangeTrackers != null) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < sectionedBlockChangeTrackers.size(); i++) {
                sectionedBlockChangeTrackers.get(i).setChanged(section);
            }
        }
    }

    public void addTracker(SectionedBlockChangeTracker tracker) {
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers;
        if (sectionedBlockChangeTrackers == null) {
            this.trackers = (sectionedBlockChangeTrackers = new ArrayList<>());
        }
        sectionedBlockChangeTrackers.add(tracker);
    }

    public void removeTracker(SectionedBlockChangeTracker tracker) {
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers;
        if (sectionedBlockChangeTrackers != null) {
            sectionedBlockChangeTrackers.remove(tracker);
        }
    }

    public void onChunkSectionInvalidated(SectionPos sectionPos) {
        ArrayList<SectionedBlockChangeTracker> sectionedBlockChangeTrackers = this.trackers;
        this.trackers = null;
        if (sectionedBlockChangeTrackers != null) {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < sectionedBlockChangeTrackers.size(); i++) {
                sectionedBlockChangeTrackers.get(i).onChunkSectionInvalidated(sectionPos);
            }
        }
    }
}
