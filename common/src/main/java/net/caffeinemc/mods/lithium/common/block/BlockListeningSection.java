package net.caffeinemc.mods.lithium.common.block;

import net.caffeinemc.mods.lithium.common.entity.block_tracking.SectionedBlockChangeTracker;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

public interface BlockListeningSection {

    void lithium$addToCallback(SectionedBlockChangeTracker tracker, long sectionPos, Level world);

    void lithium$removeFromCallback(SectionedBlockChangeTracker tracker);

    void lithium$invalidateListeningSection(SectionPos sectionPos);
}
