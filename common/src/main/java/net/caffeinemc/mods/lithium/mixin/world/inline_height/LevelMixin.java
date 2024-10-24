package net.caffeinemc.mods.lithium.mixin.world.inline_height;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implement world height related methods directly instead of going through WorldView and Dimension
 */
@Mixin(Level.class)
public abstract class LevelMixin implements LevelHeightAccessor {
    private int bottomY;
    private int height;
    private int topYInclusive;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void initHeightCache(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, RegistryAccess registryAccess, Holder<DimensionType> dimensionEntry, boolean bl, boolean bl2, long l, int i, CallbackInfo ci) {
        this.height = dimensionEntry.value().height();
        this.bottomY = dimensionEntry.value().minY();
        this.topYInclusive = this.bottomY + this.height - 1;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getMinY() {
        return this.bottomY;
    }

    @Override
    public int getSectionsCount() {
        return ((this.topYInclusive >> 4) + 1) - (this.bottomY >> 4);
    }

    @Override
    public int getMinSectionY() {
        return this.bottomY >> 4;
    }

    @Override
    public int getMaxSectionY() {
        return (this.topYInclusive >> 4) + 1;
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        int y = pos.getY();
        return (y < this.bottomY) || (y > this.topYInclusive);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return (y < this.bottomY) || (y > this.topYInclusive);
    }

    @Override
    public int getSectionIndex(int y) {
        return (y >> 4) - (this.bottomY >> 4);
    }

    @Override
    public int getSectionIndexFromSectionY(int coord) {
        return coord - (this.bottomY >> 4);

    }

    @Override
    public int getSectionYFromSectionIndex(int index) {
        return index + (this.bottomY >> 4);
    }

    @Override
    public int getMaxY() {
        return this.topYInclusive;
    }
}