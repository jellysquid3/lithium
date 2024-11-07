package net.caffeinemc.mods.lithium.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface ShapeUpdateHandlingBlockBehaviour {

    default void lithium$handleShapeUpdate(LevelReader world, BlockState myBlockState, BlockPos myPos, BlockPos posFrom, BlockState newState) {
    }

}
