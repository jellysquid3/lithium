package net.caffeinemc.mods.lithium.mixin.ai.pathing;

import net.caffeinemc.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(
            method = "bootStrap",
            at = @At("RETURN")
    )
    private static void afterBootstrap(CallbackInfo ci) {
        for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
            ((BlockStatePathingCache) blockState).lithium$initializePathNodeTypeCache();
        }
    }
}
