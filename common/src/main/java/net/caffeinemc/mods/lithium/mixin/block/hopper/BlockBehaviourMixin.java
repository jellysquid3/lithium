package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.common.block.entity.ShapeUpdateHandlingBlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin implements ShapeUpdateHandlingBlockBehaviour {

    @Inject(method = "updateShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/world/level/ScheduledTickAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"))
    private void notifyOnShapeUpdate(BlockState myBlockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos myPos, Direction direction, BlockPos posFrom, BlockState newState, RandomSource randomSource, CallbackInfoReturnable<BlockState> cir) {
        //Triggers when a shape update (= update that observers can detect) is sent
        this.lithium$handleShapeUpdate(levelReader, myBlockState, myPos, posFrom, newState);
    }
}
