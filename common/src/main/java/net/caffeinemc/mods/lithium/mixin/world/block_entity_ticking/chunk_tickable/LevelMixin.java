package net.caffeinemc.mods.lithium.mixin.world.block_entity_ticking.chunk_tickable;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public class LevelMixin {

    @Redirect(
            method = "tickBlockEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;shouldTickBlocksAt(Lnet/minecraft/core/BlockPos;)Z")
    )
    private boolean optimizedShouldTick(Level instance, BlockPos pos, @Share("ShouldTickPos") LocalLongRef lastTickableChunk) {
        if (pos == null) {
            return false;
        }
        long chunkPos = ChunkPos.asLong(pos);
        if (chunkPos == lastTickableChunk.get()) {
            return true;
        }
        boolean b = instance.shouldTickBlocksAt(chunkPos);
        if (b) {
            lastTickableChunk.set(chunkPos);
        }
        return b;
    }
}
