package net.caffeinemc.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public class LevelMixin {

    @WrapOperation(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;shouldTickBlocksAt(Lnet/minecraft/core/BlockPos;)Z"),
            require = 0
    )
    private boolean shouldTickBlockPosFilterNull(Level instance, BlockPos pos, Operation<Boolean> original) {
        if (pos == null) {
            return false;
        }
        return original.call(instance, pos);
    }
}
