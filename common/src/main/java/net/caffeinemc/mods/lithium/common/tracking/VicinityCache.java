package net.caffeinemc.mods.lithium.common.tracking;

import it.unimi.dsi.fastutil.objects.Reference2DoubleArrayMap;
import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.caffeinemc.mods.lithium.common.tracking.entity.SectionedColliderEntityMovementTracker;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class VicinityCache {
    // To avoid slowing down setblock operations, only start caching after 1.5 Seconds = 30 gameticks with estimated 6 accesses per tick
    private static final int MIN_DELAY = 30 * 6;
    private int initDelay; //Changing MIN_DELAY should not affect correctness, just performance in some cases

    private AABB trackedPos;
    private SectionedBlockChangeTracker blockTracker;
    private SectionedColliderEntityMovementTracker collisionEntityTracker;
    private long trackingBlocksSince;
    private long trackingCollisionEntitiesSince;

    private boolean canSkipSupportingBlockSearch;
    private BlockState cachedSupportingBlock;

    private boolean canSkipBlockTouching;
    //0 if not touching fire/lava. 1 if touching fire/lava. -1 if not cached
    private byte cachedTouchingFireLava;
    //0 if not suffocating. 1 if touching suffocating. -1 if not cached
    private byte cachedIsSuffocating;
    //Touched fluid's height IF fluid pushing is 0. Touched fluid height is 0 when not touching that fluid. Not in collection: No cached value (uninitialized OR fluid pushing is not 0)
    private final Reference2DoubleArrayMap<TagKey<Fluid>> fluidType2FluidHeightMap;
    //Last failed movement vector(s)
    private Vec3 cachedFailedMovement;

    public VicinityCache() {
        this.blockTracker = null;
        this.trackedPos = null;
        this.initDelay = 0;
        this.fluidType2FluidHeightMap = new Reference2DoubleArrayMap<>(2);
    }

    public boolean isTrackingBlocks() {
        return this.blockTracker != null;
    }

    public boolean isTrackingCollisionEntities() {
        return this.collisionEntityTracker != null;
    }

    public void initTrackingBlocks(Entity entity) {
        if (this.isTrackingBlocks()) {
            throw new IllegalStateException("Cannot init cache that is already initialized!");
        }
        this.blockTracker = SectionedBlockChangeTracker.registerAtForCollisions(entity.level(), entity.getBoundingBox());
        this.initDelay = 0;
        this.resetCache();
    }

    public void initTrackingEntities(Entity entity) {
        if (this.isTrackingCollisionEntities()) {
            throw new IllegalStateException("Cannot init cache that is already initialized!");
        }
        this.collisionEntityTracker = SectionedColliderEntityMovementTracker.registerAt(entity.level(), entity.getBoundingBox());
        this.initDelay = 0;
        this.resetCache();
    }

    public void updateCacheBlocksOnly(Entity entity) {
        if (this.isTrackingBlocks() || this.initDelay >= MIN_DELAY) {
            if (isStationaryOtherwiseUpdate(entity)) {
                if (!this.isTrackingBlocks()) {
                    this.initTrackingBlocks(entity);
                } else if (!this.blockTracker.isUnchangedSince(this.trackingBlocksSince)) {
                    this.resetCache();
                }
            }
        } else {
            this.initDelay++;
        }
    }

    public void updateCacheBlocksAndEntities(Entity entity) {
        if (this.isTrackingBlocks() && this.isTrackingCollisionEntities() || this.initDelay >= MIN_DELAY) {
            if (isStationaryOtherwiseUpdate(entity)) {
                if (!this.isTrackingBlocks()) {
                    this.initTrackingBlocks(entity);
                } else if (!this.blockTracker.isUnchangedSince(this.trackingBlocksSince)) {
                    this.resetCache();
                } else if (!this.isTrackingCollisionEntities()) {
                    this.initTrackingEntities(entity);
                } else if (!this.collisionEntityTracker.isUnchangedSince(this.trackingCollisionEntitiesSince)) {
                    this.resetEntityAndBlockDependentCache();
                }
            }
        } else {
            this.initDelay++;
        }
    }

    private boolean isStationaryOtherwiseUpdate(Entity entity) {
        AABB boundingBox = entity.getBoundingBox();
        boolean stationary = boundingBox.equals(this.trackedPos);
        if (!stationary) {
            this.shiftPosition(entity, boundingBox);
        }
        return stationary;
    }

    private void shiftPosition(Entity entity, AABB boundingBox) {
        if (this.isTrackingBlocks() && !this.blockTracker.matchesMovedBox(boundingBox)) {
            this.blockTracker.unregister();
            this.blockTracker = null;
        }
        if (this.isTrackingCollisionEntities() && !this.collisionEntityTracker.matchesMovedBox(boundingBox)) {
            this.collisionEntityTracker.unRegister(entity.level());
            this.collisionEntityTracker = null;
        }
        this.resetTrackedPos(boundingBox);
    }

    private void resetTrackedPos(AABB boundingBox) {
        this.trackedPos = boundingBox;
        this.initDelay = 0;
        this.resetCache();
    }

    private void resetCache() {
        this.trackingBlocksSince = !this.isTrackingBlocks() ? Long.MIN_VALUE : this.blockTracker.getWorldTime();
        this.canSkipSupportingBlockSearch = false;
        this.cachedSupportingBlock = null;
        this.cachedIsSuffocating = (byte) -1;
        this.cachedTouchingFireLava = (byte) -1;
        this.canSkipBlockTouching = false;
        this.fluidType2FluidHeightMap.clear();
        this.resetEntityAndBlockDependentCache();
    }

    private void resetEntityAndBlockDependentCache() {
        this.trackingCollisionEntitiesSince = !this.isTrackingCollisionEntities() ? Long.MIN_VALUE : this.collisionEntityTracker.getWorldTime();
        this.cachedFailedMovement = null;
    }

    public void remove() {
        if (this.blockTracker != null) {
            this.blockTracker.unregister();
        }
    }

    public boolean canSkipBlockTouching() {
        return this.isTrackingBlocks() && this.canSkipBlockTouching;
    }

    public void setCanSkipBlockTouching(boolean value) {
        this.canSkipBlockTouching = value;
    }

    public double getStationaryFluidHeightOrDefault(TagKey<Fluid> fluid, double defaultValue) {
        if (this.isTrackingBlocks()) {
            return this.fluidType2FluidHeightMap.getOrDefault(fluid, defaultValue);
        }
        return defaultValue;
    }

    public void setCachedFluidHeight(TagKey<Fluid> fluid, double fluidHeight) {
        this.fluidType2FluidHeightMap.put(fluid, fluidHeight);
    }

    public byte getIsTouchingFireLava() {
        if (this.isTrackingBlocks()) {
            return this.cachedTouchingFireLava;
        }
        return (byte) -1;
    }

    public void setCachedTouchingFireLava(boolean b) {
        this.cachedTouchingFireLava = b ? (byte) 1 : (byte) 0;
    }

    public byte getIsSuffocating() {
        if (this.isTrackingBlocks()) {
            return this.cachedIsSuffocating;
        }
        return (byte) -1;
    }

    public void setCachedIsSuffocating(boolean b) {
        this.cachedIsSuffocating = b ? (byte) 1 : (byte) 0;
    }

    public boolean canSkipSupportingBlockSearch() {
        return this.isTrackingBlocks() && this.canSkipSupportingBlockSearch;
    }

    public void setCanSkipSupportingBlockSearch(boolean canSkip) {
        this.canSkipSupportingBlockSearch = canSkip;
        this.cachedSupportingBlock = null;
    }

    public void cacheSupportingBlockState(BlockState blockState) {
        this.cachedSupportingBlock = blockState;
    }

    public BlockState getCachedSupportingBlock() {
        if (!this.isTrackingBlocks()) {
            return null;
        }
        return this.cachedSupportingBlock;
    }

    public Vec3 getCachedFailedMovement() {
        if (this.isTrackingBlocks() && this.isTrackingCollisionEntities()) {
            return this.cachedFailedMovement;
        }
        return null;
    }

    public void setCachedFailedMovement(Vec3 cachedFailedMovement) {
        this.cachedFailedMovement = cachedFailedMovement;
    }
}
