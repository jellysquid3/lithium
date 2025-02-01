package net.caffeinemc.mods.lithium.mixin.block_pattern_matching;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.world.block_pattern_matching.BlockPatternExtended;
import net.caffeinemc.mods.lithium.common.world.block_pattern_matching.BlockSearch;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPattern.class)
public class BlockPatternMixin implements BlockPatternExtended {


    @Unique
    private Block requiredBlock;
    @Unique
    private int requiredBlockCount;

    @Inject(
            method = "find", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;"), cancellable = true
    )
    private void countRequiredBlocksBeforeExpensiveSearch(LevelReader levelReader, BlockPos blockPos, CallbackInfoReturnable<BlockPattern.BlockPatternMatch> cir, @Local int size) {
        if (this.requiredBlock != null) {
            BlockPos maxCorner = blockPos.offset(2 * size - 1, 2 * size - 1, 2 * size - 1);
            BlockPos minCorner = blockPos.offset(-size, -size, -size);

            BlockBox searchBox = BlockBox.of(minCorner, maxCorner);
            if (!BlockSearch.hasAtLeast(levelReader, searchBox, this.requiredBlock, this.requiredBlockCount)) {
                cir.setReturnValue(null);
            }
        }
    }

    @Override
    public void lithium$setRequiredBlock(Block block, int count) {
        this.requiredBlock = block;
        this.requiredBlockCount = count;
    }
}
