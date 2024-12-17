package net.caffeinemc.mods.lithium.mixin.ai.sensor.replace_streams.tempting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TemptingSensor.class)
public abstract class TemptingSensorMixin {

    @Shadow
    @Final
    private static TargetingConditions TEMPT_TARGETING;

    @Shadow
    protected abstract boolean playerHoldingTemptation(Player player);

    /**
     * @author 2No2Name
     * @reason Replace Stream code
     */
    @Overwrite
    public void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        Brain<?> brain = pathfinderMob.getBrain();
        TargetingConditions targetingConditions = TEMPT_TARGETING.copy().range((float)pathfinderMob.getAttributeValue(Attributes.TEMPT_RANGE));
        ServerPlayer closestPlayer = null;
        double minDist = Double.MAX_VALUE;

        for (ServerPlayer serverPlayer : serverLevel.players()) {
            if (EntitySelector.NO_SPECTATORS.test(serverPlayer)) {
                if (targetingConditions.test(serverLevel, pathfinderMob, serverPlayer)) {
                    if (playerHoldingTemptation(serverPlayer)) {
                        if (!pathfinderMob.hasPassenger(serverPlayer)) {
                            double dist = pathfinderMob.distanceToSqr(serverPlayer);
                            if (dist < minDist) {
                                minDist = dist;
                                closestPlayer = serverPlayer;
                            }
                        }
                    }
                }
            }
        }
        if (closestPlayer != null) {
            brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, closestPlayer);
        } else {
            brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }
    }
}
