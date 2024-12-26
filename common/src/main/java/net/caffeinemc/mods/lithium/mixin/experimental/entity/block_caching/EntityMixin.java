package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching;

import net.caffeinemc.mods.lithium.common.tracking.VicinityCache;
import net.caffeinemc.mods.lithium.common.tracking.VicinityCacheProvider;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements VicinityCacheProvider {
    @Unique
    private final VicinityCache vicinityCache = new VicinityCache();

    @Override
    public VicinityCache lithium$getVicinityCache() {
        return this.vicinityCache;
    }

    @Inject(
            method = "remove",
            at = @At("HEAD")
    )
    private void removeVicinityCache(Entity.RemovalReason reason, CallbackInfo ci) {
        this.vicinityCache.remove();
    }
}
