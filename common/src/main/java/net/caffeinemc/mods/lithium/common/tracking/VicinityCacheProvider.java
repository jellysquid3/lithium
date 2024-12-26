package net.caffeinemc.mods.lithium.common.tracking;

import net.minecraft.world.entity.Entity;

public interface VicinityCacheProvider {
    VicinityCache lithium$getVicinityCache();

    default VicinityCache getUpdatedVicinityCache(Entity entity) {
        VicinityCache bc = this.lithium$getVicinityCache();
        bc.updateCache(entity);
        return bc;
    }
}
