package net.caffeinemc.mods.lithium.common.tracking;

import net.minecraft.world.entity.Entity;

public interface VicinityCacheProvider {
    VicinityCache lithium$getVicinityCache();

    default VicinityCache getUpdatedVicinityCacheForBlocks(Entity entity) {
        VicinityCache bc = this.lithium$getVicinityCache();
        bc.updateCacheBlocksOnly(entity);
        return bc;
    }

    default VicinityCache getUpdatedVicinityCacheForBlocksAndCollisionEntities(Entity entity) {
        VicinityCache bc = this.lithium$getVicinityCache();
        bc.updateCacheBlocksAndEntities(entity);
        return bc;
    }
}
