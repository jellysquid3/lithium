package net.caffeinemc.mods.lithium.common.block;

import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.minecraft.world.level.Level;

public interface BlockListeningSection {

    void lithium$addToCallback(SectionedBlockChangeTracker tracker, long sectionPos, Level world);

    void lithium$removeFromCallback(SectionedBlockChangeTracker tracker);

}
