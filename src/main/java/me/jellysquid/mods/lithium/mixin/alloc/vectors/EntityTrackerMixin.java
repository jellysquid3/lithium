package me.jellysquid.mods.lithium.mixin.alloc.vectors;

import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Avoid unnecessary Vec3d allocations
@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public class EntityTrackerMixin {

    @Shadow
    @Final
    EntityTrackerEntry entry;

    @Redirect(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;subtract(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;")
    )
    private Vec3d removeAllocation(Vec3d playerPos, Vec3d playerLastPos) {
        return null;
    }

    @Redirect(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;x:D")
    )
    private double calcX(Vec3d vec, ServerPlayerEntity player) {
        return player.getPos().x - entry.getLastPos().x;
    }

    @Redirect(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;z:D")
    )
    private double calcZ(Vec3d vec, ServerPlayerEntity player) {
        return player.getPos().z - this.entry.getLastPos().z;
    }
}
