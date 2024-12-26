package net.caffeinemc.mods.lithium.mixin.ai.pathing;

import net.caffeinemc.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hook into the initialization of fuel values, because those are always updated after block tags are modified,
 * and we must update our cache path node information which is also based on block tags.
 */
@Mixin(FuelValues.class)
public class FuelValuesMixin {

    @Inject(
            method = "vanillaBurnTimes(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;I)Lnet/minecraft/world/level/block/entity/FuelValues;",
            at = @At(value = "RETURN")
    )
    private static void initializeCachedBlockData(HolderLookup.Provider provider, FeatureFlagSet featureFlagSet, int i, CallbackInfoReturnable<FuelValues> cir) {
        // Initialize / Reinitialize the cached path node types.
        // This is called before burn times are set in the minecraft server, but we don't care about the updated burn times
        for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
            ((BlockStatePathingCache) blockState).lithium$initializePathNodeTypeCache();
        }
    }
}
