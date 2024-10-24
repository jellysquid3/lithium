package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching.block_touching;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlagHolder;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlags;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCache;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCacheProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

/**
 * This mixin uses the block caching system to be able to skip entity block interactions when the entity is not a player
 * and the nearby blocks cannot be interacted with by touching them.
 */
@Mixin(Entity.class)
public abstract class EntityMixin implements BlockCacheProvider {
    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"), cancellable = true
    )
    private void cancelIfSkippable(List<?> movementList, Set<BlockState> stateCollector, CallbackInfo ci) {
        if (movementList.size() != 1) {
            return; // If there are multiple movements, blocks far away, outside the cached range are queried.
            // Not sure whether this special case is really needed, but it's here to be safe.
        }
        //noinspection ConstantConditions
        if (!((Object) this instanceof ServerPlayer)) {
            BlockCache bc = this.getUpdatedBlockCache((Entity) (Object) this);
            if (bc.canSkipBlockTouching()) {
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;deflate(D)Lnet/minecraft/world/phys/AABB;")
    )
    private void assumeNoTouchableBlock(CallbackInfo ci) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.isTracking()) {
            bc.setCanSkipBlockTouching(true);
        }
    }

    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "RETURN")
    )
    private void checkTouchableBlock(CallbackInfo ci, @Local(argsOnly = true) Set<BlockState> set) {
        BlockCache bc = this.lithium$getBlockCache();
        if (bc.canSkipBlockTouching()) {
            for (BlockState blockState : set) {
                if (0 != (((BlockStateFlagHolder) blockState).lithium$getAllFlags() & 1 << BlockStateFlags.ENTITY_TOUCHABLE.getIndex())) {
                    bc.setCanSkipBlockTouching(false);
                    break;
                }
            }
        }
    }
}
