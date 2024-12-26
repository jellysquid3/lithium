package net.caffeinemc.mods.lithium.common.tracking.block;

import net.minecraft.world.entity.Entity;

public interface BlockCacheProvider {
    BlockCache lithium$getBlockCache();

    default BlockCache getUpdatedBlockCache(Entity entity) {
        BlockCache bc = this.lithium$getBlockCache();
        bc.updateCache(entity);
        return bc;
    }
}
