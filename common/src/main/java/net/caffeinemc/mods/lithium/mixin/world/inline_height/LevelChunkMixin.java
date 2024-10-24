package net.caffeinemc.mods.lithium.mixin.world.inline_height;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements LevelHeightAccessor {

    @Shadow
    @Final
    Level level;

    @Override
    public int getMaxY() {
        return this.level.getMaxY();
    }

    @Override
    public int getMinSectionY() {
        return this.level.getMinSectionY();
    }

    @Override
    public int getMaxSectionY() {
        return this.level.getMaxSectionY();
    }

    @Override
    public boolean isInsideBuildHeight(int i) {
        return this.level.isInsideBuildHeight(i);
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getSectionsCount() {
        return this.level.getSectionsCount();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return this.level.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return this.level.isOutsideBuildHeight(y);
    }

    @Override
    public int getSectionIndex(int y) {
        return this.level.getSectionIndex(y);
    }

    @Override
    public int getSectionIndexFromSectionY(int coord) {
        return this.level.getSectionIndexFromSectionY(coord);
    }

    @Override
    public int getSectionYFromSectionIndex(int index) {
        return this.level.getSectionYFromSectionIndex(index);
    }
}
