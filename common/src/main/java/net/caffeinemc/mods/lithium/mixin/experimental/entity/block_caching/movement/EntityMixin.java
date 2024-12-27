package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching.movement;

import net.caffeinemc.mods.lithium.common.entity.EntityClassGroup;
import net.caffeinemc.mods.lithium.common.tracking.VicinityCache;
import net.caffeinemc.mods.lithium.common.tracking.VicinityCacheProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements VicinityCacheProvider {

    @Shadow
    private Level level;

    @Inject(
            method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"), cancellable = true
    )
    private void tryCancelMovement(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        if (EntityClassGroup.CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(this.getClass())) {
            return; //The tracking system only tracks entities with hard hitboxes, but the current entity can collide with a lot more
        }
        VicinityCache bc = this.lithium$getUpdatedVicinityCacheForBlocksAndCollisionEntities((Entity) (Object) this);
        if (movement.equals(bc.getCachedFailedMovement())) {
            cir.setReturnValue(Vec3.ZERO);
        }
    }

    @Inject(
            method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN")
    )
    private void cacheFailedMovement(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        if (cir.getReturnValue() == Vec3.ZERO) {
            if (EntityClassGroup.CUSTOM_COLLIDE_LIKE_MINECART_BOAT_WINDCHARGE.contains(this.getClass())) {
                return; //The tracking system only tracks entities with hard hitboxes, but the current entity can collide with a lot more
            }
            VicinityCache bc = this.lithium$getVicinityCache();
            //Do not cache anything if the world border may be responsible for the movement failing. The world border
            // might change, but we are not detecting it.
            if (bc.isTrackingCollisionEntities() && bc.isTrackingBlocks() && !this.isMaybeCollidingWithWorldBorder()) {
                bc.setCachedFailedMovement(movement);
            }
        }
    }

    @Unique
    private boolean isMaybeCollidingWithWorldBorder() {
        return this.level.getWorldBorder().getDistanceToBorder((Entity) (Object) this) < 3.0;
    }
}
