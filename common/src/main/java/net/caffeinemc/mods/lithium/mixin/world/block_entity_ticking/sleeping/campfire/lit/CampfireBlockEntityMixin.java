package net.caffeinemc.mods.lithium.mixin.world.block_entity_ticking.sleeping.campfire.lit;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.block.entity.SleepingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(
            method = "cookTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;)V",
            at = @At("RETURN")
    )
    private static void trySleepLit(CallbackInfo ci, @Local(argsOnly = true) CampfireBlockEntity campfireBlockEntity, @Local boolean bl) {
        if (!bl) {
            CampfireBlockEntityMixin self = (CampfireBlockEntityMixin) (Object) campfireBlockEntity;
            //noinspection DataFlowIssue
            self.lithium$startSleeping();
        }
    }
}
