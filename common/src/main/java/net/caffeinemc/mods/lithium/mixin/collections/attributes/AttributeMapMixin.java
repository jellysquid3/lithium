package net.caffeinemc.mods.lithium.mixin.collections.attributes;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(AttributeMap.class)
public class AttributeMapMixin {

    @Mutable
    @Shadow
    @Final
    private Set<AttributeInstance> attributesToUpdate;

    @Mutable
    @Shadow
    @Final
    private Set<AttributeInstance> attributesToSync;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initCollections(AttributeSupplier defaultAttributes, CallbackInfo ci) {
        this.attributesToUpdate = new ReferenceOpenHashSet<>(0);
        this.attributesToSync = new ReferenceOpenHashSet<>(0);
    }
}
