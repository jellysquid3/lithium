package net.caffeinemc.mods.lithium.mixin.util.block_tracking;

import net.caffeinemc.mods.lithium.common.block.BlockStateFlagHolder;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Bootstrap.class, priority = 1010)
public class BootstrapMixin {
    @Inject(
            method = "bootStrap",
            at = @At("RETURN")
    )
    private static void afterBootstrap(CallbackInfo ci) {
        for (BlockState blockState : Block.BLOCK_STATE_REGISTRY) {
            ((BlockStateFlagHolder) blockState).lithium$initializeFlags();
        }
    }
}
