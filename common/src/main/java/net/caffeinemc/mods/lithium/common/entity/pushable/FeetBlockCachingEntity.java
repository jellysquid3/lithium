package net.caffeinemc.mods.lithium.common.entity.pushable;

import net.minecraft.world.level.block.state.BlockState;

public interface FeetBlockCachingEntity {

    default void lithium$OnFeetBlockCacheDeleted() {

    }

    default void lithium$OnFeetBlockCacheSet(BlockState newState) {

    }

    default void lithium$SetClimbingMobCachingSectionUpdateBehavior(boolean listening) {
        throw new UnsupportedOperationException();
    }

    BlockState lithium$getCachedFeetBlockState();
}