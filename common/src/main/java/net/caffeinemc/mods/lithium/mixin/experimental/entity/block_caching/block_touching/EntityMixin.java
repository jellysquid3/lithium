package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching.block_touching;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlagHolder;
import net.caffeinemc.mods.lithium.common.block.BlockStateFlags;
import net.caffeinemc.mods.lithium.common.tracking.VicinityCache;
import net.caffeinemc.mods.lithium.common.tracking.VicinityCacheProvider;
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
public abstract class EntityMixin implements VicinityCacheProvider {
    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;makeBoundingBox(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/AABB;"), cancellable = true
    )
    private void cancelIfSkippable(List<?> movementList, Set<BlockState> touchedBlocks, CallbackInfo ci) {
        if (movementList.size() != 1) {
            return; // If there are multiple movements, blocks far away, outside the cached range are queried.
            // Not sure whether this special case is really needed, but it's here to be safe.
        }
        //noinspection ConstantConditions
        if (!((Object) this instanceof ServerPlayer)) {
            VicinityCache bc = this.getUpdatedVicinityCacheForBlocks((Entity) (Object) this);
            if (bc.canSkipBlockTouching()) {
                ci.cancel();
                //TODO This could lead to mod compat issues with other mods, if they implement something similar to
                // vanilla's FIRE / LAVA check. To work around this, other mods
                // have to override the method where blocks interact with entities that touch it.
                // Mojmap (1.21.4): entityInside . If mapping changed, look up how cactus damages entities, implement the same
                // method on the modded block, even if it is empty.
                // The method name is also defined at net.caffeinemc.mods.lithium.common.reflection.ReflectionUtil.REMAPPED_ON_ENTITY_COLLISION:
            }
        }
    }

    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;deflate(D)Lnet/minecraft/world/phys/AABB;")
    )
    private void assumeNoTouchableBlock(CallbackInfo ci) {
        VicinityCache bc = this.lithium$getVicinityCache();
        if (bc.isTrackingBlocks()) {
            bc.setCanSkipBlockTouching(true);
        }
    }

    @Inject(
            method = "checkInsideBlocks(Ljava/util/List;Ljava/util/Set;)V",
            at = @At(value = "RETURN")
    )
    private void checkTouchableBlock(CallbackInfo ci, @Local(argsOnly = true) Set<BlockState> set) {
        VicinityCache bc = this.lithium$getVicinityCache();
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
