package net.caffeinemc.mods.lithium.mixin.util.block_tracking;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.lithium.common.block.*;
import net.caffeinemc.mods.lithium.common.tracking.block.ChunkSectionChangeCallback;
import net.caffeinemc.mods.lithium.common.tracking.block.SectionedBlockChangeTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keep track of how many blocks that meet certain criteria are in this chunk section.
 * E.g. if no over-sized blocks are there, collision code can skip a few blocks.
 *
 * @author 2No2Name
 */
@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin implements BlockCountingSection, BlockListeningSection {

    @Shadow
    @Final
    private PalettedContainer<BlockState> states;

    @Unique
    private short[] countsByFlag = null;
    @Unique
    private ChunkSectionChangeCallback changeListener;

    @Unique
    private static void addToFlagCount(short[] countsByFlag, BlockState state, short change) {
        int flags = ((BlockStateFlagHolder) state).lithium$getAllFlags();
        int i;
        while ((i = Integer.numberOfTrailingZeros(flags)) < 32 && i < countsByFlag.length) {
            //either count up by one (prevFlag not set) or down by one (prevFlag set)
            countsByFlag[i] += change;
            flags &= ~(1 << i);
        }
    }

    @Override
    public boolean lithium$mayContainAny(TrackedBlockStatePredicate trackedBlockStatePredicate) {
        if (this.countsByFlag == null) {
            fastInitClientCounts();
        }
        return this.countsByFlag[trackedBlockStatePredicate.getIndex()] != (short) 0;
    }

    @Unique
    private void fastInitClientCounts() {
        this.countsByFlag = new short[BlockStateFlags.NUM_TRACKED_FLAGS];
        for (TrackedBlockStatePredicate trackedBlockStatePredicate : BlockStateFlags.TRACKED_FLAGS) {
            if (this.states.maybeHas(trackedBlockStatePredicate)) {
                //We haven't counted, so we just set the count so high that it never incorrectly reaches 0.
                //For most situations, this overestimation does not hurt client performance compared to correct counting,
                this.countsByFlag[trackedBlockStatePredicate.getIndex()] = 16 * 16 * 16;
            }
        }
    }

    @Redirect(
            method = "recalcBlockCounts()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/PalettedContainer;count(Lnet/minecraft/world/level/chunk/PalettedContainer$CountConsumer;)V"
            )
    )
    private void initFlagCounters(PalettedContainer<BlockState> palettedContainer, PalettedContainer.CountConsumer<BlockState> consumer) {
        palettedContainer.count((state, count) -> {
            consumer.accept(state, count);
            addToFlagCount(this.countsByFlag, state, (short) count);
        });
    }

    @Inject(method = "recalcBlockCounts()V", at = @At("HEAD"))
    private void createFlagCounters(CallbackInfo ci) {
        this.countsByFlag = new short[BlockStateFlags.NUM_TRACKED_FLAGS];
    }

    @Inject(
            method = "read(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(value = "HEAD")
    )
    private void resetData(FriendlyByteBuf buf, CallbackInfo ci) {
        this.countsByFlag = null;
    }

    @Inject(
            method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;",
                    ordinal = 0
            )
    )
    private void updateFlagCounters(int x, int y, int z, BlockState newState, boolean lock, CallbackInfoReturnable<BlockState> cir, @Local(ordinal = 1) BlockState oldState) {
        this.lithium$trackBlockStateChange(newState, oldState);
        if (this.changeListener != null) {
            this.changeListener.onBlockChange(this);
        }
    }

    @Override
    public void lithium$trackBlockStateChange(BlockState newState, BlockState oldState) {
        short[] countsByFlag = this.countsByFlag;
        if (countsByFlag == null) {
            return;
        }
        int prevFlags = ((BlockStateFlagHolder) oldState).lithium$getAllFlags();
        int flags = ((BlockStateFlagHolder) newState).lithium$getAllFlags();

        int flagsXOR = prevFlags ^ flags;
        //iterate over indices that changed and update each counter
        int flagIndex;

        while ((flagIndex = Integer.numberOfTrailingZeros(flagsXOR)) < 32 && flagIndex < countsByFlag.length) {
            int flagBit = 1 << flagIndex;
            //either count up by one (prevFlag not set) or down by one (prevFlag set)
            if ((flagsXOR & flagBit) != 0) {
                countsByFlag[flagIndex] += (short) (1 - (((prevFlags >>> flagIndex) & 1) << 1));
            }
            flagsXOR &= ~flagBit;
        }
    }

    @Override
    public void lithium$addToCallback(SectionedBlockChangeTracker tracker, long sectionPos, Level world) {
        if (this.changeListener == null) {
            if (sectionPos == Long.MIN_VALUE || world == null) {
                throw new IllegalArgumentException("Expected world and section pos during intialization!");
            }
            this.changeListener = ChunkSectionChangeCallback.create(sectionPos, world);
        }

        this.changeListener.addTracker(tracker);
    }

    @Override
    public void lithium$removeFromCallback(SectionedBlockChangeTracker tracker) {
        if (this.changeListener != null) {
            this.changeListener.removeTracker(tracker);
        }
    }

}
