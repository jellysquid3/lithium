package net.caffeinemc.mods.lithium.common.util.tuples;

import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

//Y values use coordinates, not indices (y=0 -> chunkY=0)
//upper bounds are EXCLUSIVE
public record WorldSectionBox(Level world, int chunkX1, int chunkY1, int chunkZ1, int chunkX2, int chunkY2,
                              int chunkZ2) {

    // Entity access box for possible entity intersections (AABB intersection). Not big enough for touching (equal coordinates).
    public static WorldSectionBox entityAccessBox(Level world, AABB box) { //TODO rename for clarity
        int minX = SectionPos.posToSectionCoord(box.minX - 2.0D);
        int minY = SectionPos.posToSectionCoord(box.minY - 4.0D);
        int minZ = SectionPos.posToSectionCoord(box.minZ - 2.0D);
        int maxX = SectionPos.posToSectionCoord(box.maxX + 2.0D) + 1;
        int maxY = SectionPos.posToSectionCoord(box.maxY) + 1;
        int maxZ = SectionPos.posToSectionCoord(box.maxZ + 2.0D) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    // For entity movement collisions, we have to consider 1e-7 margins. Expand by something bigger than 1e-7 in all directions to be safe
    public static WorldSectionBox entityCollisionAccessBox(Level world, AABB box) {
        int minX = SectionPos.posToSectionCoord(box.minX - 2.0D - 1e-6);
        int minY = SectionPos.posToSectionCoord(box.minY - 4.0D - 1e-6);
        int minZ = SectionPos.posToSectionCoord(box.minZ - 2.0D - 1e-6);
        int maxX = SectionPos.posToSectionCoord(box.maxX + 2.0D + 1e-6) + 1;
        int maxY = SectionPos.posToSectionCoord(box.maxY + 1e-6) + 1;
        int maxZ = SectionPos.posToSectionCoord(box.maxZ + 2.0D + 1e-6) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    //Relevant block box: Entity hitbox expanded to all blocks it touches. Then expand the resulting box by 1 block in each direction.
    //Include all chunk sections that contain blocks inside the expanded box.
    public static WorldSectionBox relevantExpandedBlocksBox(Level world, AABB box) {
        int minX = SectionPos.blockToSectionCoord(Mth.floor(box.minX) - 1);
        int minY = SectionPos.blockToSectionCoord(Mth.floor(box.minY) - 1);
        int minZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ) - 1);
        int maxX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX) + 1) + 1;
        int maxY = SectionPos.blockToSectionCoord(Mth.floor(box.maxY) + 1) + 1;
        int maxZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ) + 1) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    // For block movement collisions, we have to consider 1e-7 margins. Expand by something bigger than 1e-7 in all directions to be safe
    // Also consider the block's expected maximum hitbox, which is possibly 1 block bigger than the 1x1x1 block itself.
    // Note: the above is incorrect, as downwards moving fences / walls are 1.5 blocks bigger than the 1x1x1 block, but
    // vanilla also incorrectly assumes this to not exist.
    public static WorldSectionBox blockCollisionAccessBox(Level world, AABB box) {
        int minX = SectionPos.blockToSectionCoord(Mth.floor(box.minX - 1e-6) - 1);
        int minY = SectionPos.blockToSectionCoord(Mth.floor(box.minY - 1e-6) - 1);
        int minZ = SectionPos.blockToSectionCoord(Mth.floor(box.minZ - 1e-6) - 1);
        int maxX = SectionPos.blockToSectionCoord(Mth.floor(box.maxX + 1e-6) + 1) + 1;
        int maxY = SectionPos.blockToSectionCoord(Mth.floor(box.maxY + 1e-6) + 1) + 1;
        int maxZ = SectionPos.blockToSectionCoord(Mth.floor(box.maxZ + 1e-6) + 1) + 1;
        return new WorldSectionBox(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int numSections() {
        return (this.chunkX2 - this.chunkX1) * (this.chunkY2 - this.chunkY1) * (this.chunkZ2 - this.chunkZ1);
    }

    public boolean matchesRelevantExpandedBlocksBox(AABB box) {
        return SectionPos.blockToSectionCoord(Mth.floor(box.minX) - 1) == this.chunkX1 &&
                SectionPos.blockToSectionCoord(Mth.floor(box.minY) - 1) == this.chunkY1 &&
                SectionPos.blockToSectionCoord(Mth.floor(box.minZ) - 1) == this.chunkZ1 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxX) + 1) + 1 == this.chunkX2 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxY) + 1) + 1 == this.chunkY2 &&
                SectionPos.blockToSectionCoord(Mth.ceil(box.maxZ) + 1) + 1 == this.chunkZ2;
    }

    public boolean matchesRelevantEntityAccessBox(AABB box) {
        return SectionPos.posToSectionCoord(box.minX - 2.0D) == this.chunkX1 &&
                SectionPos.posToSectionCoord(box.minY - 4.0D) == this.chunkY1 &&
                SectionPos.posToSectionCoord(box.minZ - 2.0D) == this.chunkZ1 &&
                SectionPos.posToSectionCoord(box.maxX + 2.0D) + 1 == this.chunkX2 &&
                SectionPos.posToSectionCoord(box.maxY) + 1 == this.chunkY2 &&
                SectionPos.posToSectionCoord(box.maxZ + 2.0D) + 1 == this.chunkZ2;
    }

    public boolean matchesEntityCollisionAccessBox(AABB box) {
        return SectionPos.posToSectionCoord(box.minX - 2.0D - 1e-6) == this.chunkX1 &&
                SectionPos.posToSectionCoord(box.minY - 4.0D - 1e-6) == this.chunkY1 &&
                SectionPos.posToSectionCoord(box.minZ - 2.0D - 1e-6) == this.chunkZ1 &&
                SectionPos.posToSectionCoord(box.maxX + 2.0D + 1e-6) + 1 == this.chunkX2 &&
                SectionPos.posToSectionCoord(box.maxY + 1e-6) + 1 == this.chunkY2 &&
                SectionPos.posToSectionCoord(box.maxZ + 2.0D + 1e-6) + 1 == this.chunkZ2;
    }
}
