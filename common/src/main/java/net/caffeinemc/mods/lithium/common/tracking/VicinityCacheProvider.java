package net.caffeinemc.mods.lithium.common.tracking;

import net.minecraft.world.entity.Entity;

public interface VicinityCacheProvider {
    VicinityCache lithium$getVicinityCache();

    default VicinityCache lithium$getUpdatedVicinityCacheForBlocks(Entity entity) {
        VicinityCache bc = this.lithium$getVicinityCache();
        bc.updateCacheBlocksOnly(entity);
        return bc;
    }

    default VicinityCache lithium$getUpdatedVicinityCacheForBlocksAndCollisionEntities(Entity entity) {
        VicinityCache bc = this.lithium$getVicinityCache();
        bc.updateCacheBlocksAndEntities(entity);
        return bc;
    }
}
