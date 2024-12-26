package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.common.tracking.entity.ToggleableMovementTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {

    private Vec3 beforeMoveOnRailPos;
    private int beforeMoveOnRailNotificationMask;

    protected OldMinecartBehaviorMixin(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
    }

    @Inject(
            method = "moveAlongTrack",
            at = @At("HEAD")
    )
    private void avoidNotifyingMovementListeners(ServerLevel serverLevel, CallbackInfo ci) {
        if (this instanceof Container) {
            this.beforeMoveOnRailPos = this.position();
            EntityInLevelCallback changeListener = ((EntityAccessor) this).getChangeListener();
            if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
                this.beforeMoveOnRailNotificationMask = toggleableMovementTracker.lithium$setNotificationMask(0);
            }
        }
    }

    @Inject(
            method = "moveAlongTrack",
            at = @At("RETURN")
    )
    private void notifyMovementListeners(ServerLevel serverLevel, CallbackInfo ci) {
        if (this instanceof Container) {
            EntityInLevelCallback changeListener = ((EntityAccessor) this).getChangeListener();
            if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
                this.beforeMoveOnRailNotificationMask = toggleableMovementTracker.lithium$setNotificationMask(this.beforeMoveOnRailNotificationMask);

                if (!this.beforeMoveOnRailPos.equals(this.position())) {
                    changeListener.onMove();
                }
            }
            this.beforeMoveOnRailPos = null;
        }
    }
}
