package net.caffeinemc.mods.lithium.common.world;

import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.lithium.common.tracking.block.ChunkSectionChangeCallback;
import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.caffeinemc.mods.lithium.common.tracking.entity.SectionedEntityMovementTracker;
import net.caffeinemc.mods.lithium.common.util.deduplication.LithiumInterner;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public interface LithiumData {

    record Data(
            GameEventDispatcherStorage gameEventDispatchers,

            // Cached ominous banner, must not be mutated.
            ItemStack ominousBanner,

            // Set of active mob navigations (active = have a path)
            ReferenceOpenHashSet<PathNavigation> activeNavigations,

            // Block change tracker deduplication
            LithiumInterner<SectionedBlockChangeTracker> blockChangeTrackers,

            // Entity movement tracker deduplication
            LithiumInterner<SectionedEntityMovementTracker<?>> entityMovementTrackers,

            // Block ChunkSection listeners
            Long2ReferenceOpenHashMap<ChunkSectionChangeCallback> chunkSectionChangeCallbacks
    ) {
        public Data(HolderLookup.Provider registries) {
            this(
                    new GameEventDispatcherStorage(),
                    Objects.requireNonNullElse(registries, RegistryAccess.EMPTY).lookup(Registries.BANNER_PATTERN).map(Raid::getOminousBannerInstance).orElse(null),
                    new ReferenceOpenHashSet<>(),
                    new LithiumInterner<>(),
                    new LithiumInterner<>(),
                    new Long2ReferenceOpenHashMap<>()
            );
        }
    }

    LithiumData.Data lithium$getData();
}
