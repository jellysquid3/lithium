package net.caffeinemc.mods.lithium.mixin.collections.gamerules;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(GameRules.class)
public class GameRulesMixin {
    @Mutable
    @Shadow
    @Final
    private Map<GameRules.Key<?>, GameRules.Value<?>> rules;

    @Inject(
            method = "<init>(Ljava/util/Map;Lnet/minecraft/world/flag/FeatureFlagSet;)V",
            at = @At("RETURN")
    )
    private void reinitializeMap(CallbackInfo ci) {
        this.rules = new Object2ObjectOpenHashMap<>(this.rules);
    }
}
