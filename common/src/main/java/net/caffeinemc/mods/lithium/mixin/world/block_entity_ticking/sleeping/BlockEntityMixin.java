package net.caffeinemc.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import net.caffeinemc.mods.lithium.common.block.entity.SetChangedHandlingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements SetChangedHandlingBlockEntity {

    @Inject(method = "setChanged()V", at = @At("RETURN"))
    private void handleSetChanged(CallbackInfo ci) {
        this.lithium$handleSetChanged();
    }
}
