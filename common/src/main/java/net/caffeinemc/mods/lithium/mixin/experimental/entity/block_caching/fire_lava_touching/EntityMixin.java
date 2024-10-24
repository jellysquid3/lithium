package net.caffeinemc.mods.lithium.mixin.experimental.entity.block_caching.fire_lava_touching;

import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCache;
import net.caffeinemc.mods.lithium.common.entity.block_tracking.BlockCacheProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.stream.Stream;

@Mixin(Entity.class)
public abstract class EntityMixin implements BlockCacheProvider {
    private static final Stream<BlockState> EMPTY_BLOCKSTATE_STREAM = Stream.empty();
    @Shadow
    private int remainingFireTicks;

    @Shadow
    protected abstract int getFireImmuneTicks();

    @Shadow
    public boolean wasOnFire;

    @Shadow
    public boolean isInPowderSnow;

    @Shadow
    public abstract boolean isInWaterRainOrBubble();

    @Redirect(
            method = "applyEffectsFromBlocks(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE", remap = false,
                    target = "Lcom/google/common/collect/Iterables;any(Ljava/lang/Iterable;Lcom/google/common/base/Predicate;)Z"
            )
    )
    private boolean skipFireTestIfResultDoesNotMatterOrIsCached(Iterable<BlockState> iterable, com.google.common.base.Predicate<? super BlockState> predicate) {
        // Skip scanning the blocks around the entity touches by returning null when the result does not matter
        // Return null when there is no fire / lava => the branch of noneMatch is not taken
        // Otherwise return anything non-null. Here: Stream.empty. See skipNullStream(...) below.
        // Passing null vs Stream.empty() isn't nice but necessary to avoid the slow Stream API. Also
        // [VanillaCopy] the fire / lava check and the side effects (this.fireTicks) and their conditions needed to be copied. This might affect compatibility with other mods.
        if ((this.remainingFireTicks > 0 || this.remainingFireTicks == -this.getFireImmuneTicks()) && (!this.wasOnFire || !this.isInPowderSnow && !this.isInWaterRainOrBubble())) {
            return true;
        }


        BlockCache bc = this.getUpdatedBlockCache((Entity) (Object) this);

        byte cachedTouchingFireLava = bc.getIsTouchingFireLava();
        if (cachedTouchingFireLava == (byte) 0) {
            return true;
        } else if (cachedTouchingFireLava == (byte) 1) {
            return false;
        }


        for (BlockState state : iterable) {
            if (state.is(BlockTags.FIRE) || state.is(Blocks.LAVA)) {
                bc.setCachedTouchingFireLava(true);
                return false;
            }
        }

        bc.setCachedTouchingFireLava(false);
        return true;
    }
}
