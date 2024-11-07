package net.caffeinemc.mods.lithium.mixin.block.hopper;

import net.caffeinemc.mods.lithium.common.entity.movement_tracker.ToggleableMovementTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractChestBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = AbstractChestBoat.class, priority = 2000)
// Apply this mixin after other mixins, so the fallback is our Intrinsic not being applied
public abstract class AbstractChestBoatMixin extends Entity {
    public AbstractChestBoatMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Intrinsic(displace = true)
    // Intrinsic for mod compatibility: If this injection does not work, it is not critical, only a slight performance loss.
    @Override
    public void rideTick() {
        this.tickRidingSummarizeMovementNotifications();
    }

    @Unique
    private void tickRidingSummarizeMovementNotifications() {
        EntityInLevelCallback changeListener = ((EntityAccessor) this).getChangeListener();
        if (changeListener instanceof ToggleableMovementTracker toggleableMovementTracker) {
            Vec3 beforeTickPos = this.position();
            int beforeMovementNotificationMask = toggleableMovementTracker.lithium$setNotificationMask(0);

            super.rideTick();

            toggleableMovementTracker.lithium$setNotificationMask(beforeMovementNotificationMask);

            if (!beforeTickPos.equals(this.position())) {
                changeListener.onMove();
            }
        } else {
            super.rideTick();
        }
    }
}
