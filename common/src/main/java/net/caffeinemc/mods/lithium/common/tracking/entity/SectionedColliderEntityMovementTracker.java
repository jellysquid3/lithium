package net.caffeinemc.mods.lithium.common.tracking.entity;

import net.caffeinemc.mods.lithium.common.entity.EntityClassGroup;
import net.caffeinemc.mods.lithium.common.util.tuples.WorldSectionBox;
import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class SectionedColliderEntityMovementTracker extends SectionedEntityMovementTracker<Entity> {

    public static final boolean ENABLED = false; //TODO replace constant with appropriate class instanceof interface once implemented mixins

    public SectionedColliderEntityMovementTracker(WorldSectionBox worldSectionBox) {
        super(worldSectionBox, EntityClassGroup.NoDragonClassGroup.BOAT_SHULKER_LIKE_COLLISION);
    }

    public static SectionedColliderEntityMovementTracker registerAt(Level world, AABB interactionArea) {
        WorldSectionBox worldSectionBox = WorldSectionBox.entityCollisionAccessBox(world, interactionArea);
        SectionedColliderEntityMovementTracker tracker = new SectionedColliderEntityMovementTracker(worldSectionBox);
        tracker = ((LithiumData) world).lithium$getData().entityMovementTrackers().getCanonical(tracker);

        tracker.register(world);
        return tracker;
    }

    public boolean matchesMovedBox(AABB box) {
        return this.trackedWorldSections.matchesEntityCollisionAccessBox(box);
    }
}
