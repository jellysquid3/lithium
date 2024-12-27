package net.caffeinemc.mods.lithium.common.tracking.entity;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.caffeinemc.mods.lithium.common.util.tuples.WorldSectionBox;
import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.caffeinemc.mods.lithium.common.world.WorldHelper;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntitySectionStorage;

import java.util.ArrayList;

public abstract class SectionedEntityMovementTracker<E extends EntityAccess> {
    final WorldSectionBox trackedWorldSections;
    final Object clazz; //EntityClassGroup or Entity class / superclass
    private final int trackedIndex;
    ArrayList<EntitySection<E>> sortedSections;
    boolean[] sectionVisible;
    private int timesRegistered;
    private final ArrayList<EntityMovementTrackerSection> sectionsNotListeningTo;

    private long maxChangeTime;

    private ReferenceOpenHashSet<SectionedEntityMovementListener> sectionedEntityMovementListeners;

    public SectionedEntityMovementTracker(WorldSectionBox interactionChunks, Object entityType) {
        this.clazz = entityType;
        this.trackedWorldSections = interactionChunks;
        this.trackedIndex = MovementTrackerHelper.getTrackerIndex(entityType);
        assert this.trackedIndex != -1;
        this.sectionedEntityMovementListeners = null;
        this.sectionsNotListeningTo = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return HashCommon.mix(this.trackedWorldSections.hashCode()) ^ HashCommon.mix(this.trackedIndex) ^ this.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == this.getClass() &&
                this.clazz == ((SectionedEntityMovementTracker<?>) obj).clazz &&
                this.trackedWorldSections.equals(((SectionedEntityMovementTracker<?>) obj).trackedWorldSections);
    }

    /**
     * Method to quickly check whether any relevant entities moved inside the relevant entity sections after
     * the last interaction attempt.
     *
     * @param lastCheckedTime time of the last interaction attempt
     * @return whether any relevant entity moved in the tracked area
     */
    public boolean isUnchangedSince(long lastCheckedTime) {
        if (lastCheckedTime <= this.maxChangeTime) {
            return false;
        }
        if (!this.sectionsNotListeningTo.isEmpty()) {
            this.setChanged(this.listenToAllSectionsAndGetMaxChangeTime());
            return lastCheckedTime > this.maxChangeTime;
        }
        return true;
    }

    private long listenToAllSectionsAndGetMaxChangeTime() {
        long maxChangeTime = Long.MIN_VALUE;
        ArrayList<EntityMovementTrackerSection> notListeningTo = this.sectionsNotListeningTo;
        for (int i = notListeningTo.size() - 1; i >= 0; i--) {
            EntityMovementTrackerSection entityMovementTrackerSection = notListeningTo.remove(i);
            entityMovementTrackerSection.lithium$listenToMovementOnce(this, this.trackedIndex);
            maxChangeTime = Math.max(maxChangeTime, entityMovementTrackerSection.lithium$getChangeTime(this.trackedIndex));
        }
        return maxChangeTime;
    }

    public void register(Level world) {
        assert world == this.trackedWorldSections.world();

        if (this.timesRegistered == 0) {
            EntitySectionStorage<E> cache = WorldHelper.getEntityCacheOrNull(world);

            WorldSectionBox trackedSections = this.trackedWorldSections;
            int size = trackedSections.numSections();
            assert size > 0;
            this.sortedSections = new ArrayList<>(size);
            this.sectionVisible = new boolean[size];

            //vanilla iteration order in SectionedEntityCache is xzy
            //WorldSectionBox upper coordinates are exclusive
            for (int x = trackedSections.chunkX1(); x < trackedSections.chunkX2(); x++) {
                for (int z = trackedSections.chunkZ1(); z < trackedSections.chunkZ2(); z++) {
                    for (int y = trackedSections.chunkY1(); y < trackedSections.chunkY2(); y++) {
                        EntitySection<E> section = cache.getOrCreateSection(SectionPos.asLong(x, y, z));
                        EntityMovementTrackerSection sectionAccess = (EntityMovementTrackerSection) section;
                        this.sortedSections.add(section);
                        sectionAccess.lithium$addListener(this);
                    }
                }
            }
            this.setChanged(world.getGameTime());
        }

        this.timesRegistered++;
    }

    public void unRegister(Level world) {
        assert world == this.trackedWorldSections.world();
        if (--this.timesRegistered > 0) {
            return;
        }
        assert this.timesRegistered == 0;
        EntitySectionStorage<E> cache = WorldHelper.getEntityCacheOrNull(world);
        ((LithiumData) world).lithium$getData().entityMovementTrackers().deleteCanonical(this);

        ArrayList<EntitySection<E>> sections = this.sortedSections;
        for (int i = sections.size() - 1; i >= 0; i--) {
            EntitySection<E> section = sections.get(i);
            EntityMovementTrackerSection sectionAccess = (EntityMovementTrackerSection) section;
            sectionAccess.lithium$removeListener(cache, this);
            if (!this.sectionsNotListeningTo.remove(section)) {
                ((EntityMovementTrackerSection) section).lithium$removeListenToMovementOnce(this, this.trackedIndex);
            }
        }
        this.setChanged(world.getGameTime());
    }

    /**
     * Register an entity section to this listener, so this listener can look for changes in the section.
     */
    public void onSectionEnteredRange(EntityMovementTrackerSection section) {
        this.setChanged(this.trackedWorldSections.world().getGameTime());
        //noinspection SuspiciousMethodCalls
        int sectionIndex = this.sortedSections.lastIndexOf(section);
        this.sectionVisible[sectionIndex] = true;

        this.sectionsNotListeningTo.add(section);
        this.notifyAllListeners();
    }

    public void onSectionLeftRange(EntityMovementTrackerSection section) {
        this.setChanged(this.trackedWorldSections.world().getGameTime());
        //noinspection SuspiciousMethodCalls
        int sectionIndex = this.sortedSections.lastIndexOf(section);

        this.sectionVisible[sectionIndex] = false;

        if (!this.sectionsNotListeningTo.remove(section)) {
            section.lithium$removeListenToMovementOnce(this, this.trackedIndex);
            this.notifyAllListeners();
        }
    }

    /**
     * Method that marks that new entities might have appeared or moved in the tracked chunk sections.
     */
    private void setChanged(long atTime) {
        if (atTime > this.maxChangeTime) {
            this.maxChangeTime = atTime;
        }
    }

    public void listenToEntityMovementOnce(SectionedEntityMovementListener listener) {
        if (this.sectionedEntityMovementListeners == null) {
            this.sectionedEntityMovementListeners = new ReferenceOpenHashSet<>();
        }
        this.sectionedEntityMovementListeners.add(listener);

        if (!this.sectionsNotListeningTo.isEmpty()) {
            this.setChanged(this.listenToAllSectionsAndGetMaxChangeTime());
        }

    }

    public void emitEntityMovement(int classMask, EntityMovementTrackerSection section) {
        if ((classMask & (1 << this.trackedIndex)) != 0) {
            this.notifyAllListeners();
            this.sectionsNotListeningTo.add(section);
        }
    }

    private void notifyAllListeners() {
        ReferenceOpenHashSet<SectionedEntityMovementListener> listeners = this.sectionedEntityMovementListeners;
        if (listeners != null && !listeners.isEmpty()) {
            for (SectionedEntityMovementListener listener : listeners) {
                listener.lithium$handleEntityMovement(this.clazz);
            }
            listeners.clear();
        }
    }

    public long getWorldTime() {
        return this.trackedWorldSections.world().getGameTime();
    }
}
