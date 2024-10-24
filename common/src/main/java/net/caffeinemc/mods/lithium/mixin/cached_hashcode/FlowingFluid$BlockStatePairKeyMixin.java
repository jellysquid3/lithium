package net.caffeinemc.mods.lithium.mixin.cached_hashcode;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.BlockStatePairKey.class)
public class FlowingFluid$BlockStatePairKeyMixin {
    @Shadow
    @Final
    private BlockState first;

    @Shadow
    @Final
    private BlockState second;

    @Shadow
    @Final
    private Direction direction;

    private int hash;

    /**
     * @reason Initialize the object's hashcode and cache it
     */
    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)V", at = @At("RETURN"))
    private void generateHash(BlockState blockState_1, BlockState blockState_2, Direction direction_1, CallbackInfo ci) {
        int hash = System.identityHashCode(this.first);
        hash = 31 * hash + System.identityHashCode(this.second);
        this.hash = 31 * hash + this.direction.hashCode();
    }

    /**
     * @reason Uses the cached hashcode
     * @author JellySquid
     */
    @Overwrite(remap = false)
    public int hashCode() {
        return this.hash;
    }
}
