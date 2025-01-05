package net.caffeinemc.mods.lithium.mixin.world.game_events.dispatch;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.caffeinemc.mods.lithium.common.world.GameEventDispatcherStorage;
import net.caffeinemc.mods.lithium.common.world.LithiumData;
import net.caffeinemc.mods.lithium.common.world.chunk.ChunkStatusTracker;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess {



    @Shadow
    @Final
    @Mutable
    private Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;

    public LevelChunkMixin(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable LevelChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Shadow
    public abstract Level getLevel();

    static {
        ChunkStatusTracker.registerLoadCallback((serverLevel, chunk) -> {
            GameEventDispatcherStorage dispatcherStorage =
                    ((LithiumData) serverLevel).lithium$getData().gameEventDispatchers();
            dispatcherStorage.addChunk(chunk.getPos().toLong(), ((LevelChunkMixin) (Object) chunk).gameEventListenerRegistrySections);
        });

        ChunkStatusTracker.registerUnloadCallback((serverLevel, pos) -> {
            GameEventDispatcherStorage dispatcherStorage =
                    ((LithiumData) serverLevel).lithium$getData().gameEventDispatchers();
            dispatcherStorage.removeChunk(pos.toLong());
        });
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/Heightmap$Types;values()[Lnet/minecraft/world/level/levelgen/Heightmap$Types;")
    )
    private void initGameEventDispatchers(CallbackInfo ci) {
        if (this.gameEventListenerRegistrySections.isEmpty()) {
            this.gameEventListenerRegistrySections = null;
        }
    }

    @Inject(
            method = "removeGameEventListenerRegistry",
            at = @At("RETURN")
    )
    private void removeGameEventDispatcher(int ySectionCoord, CallbackInfo ci) {
        if (this.gameEventListenerRegistrySections != null && this.gameEventListenerRegistrySections.isEmpty()) {
            this.setGameEventListenerRegistrySections(null);
        }
    }

    @Inject(
            method = "getListenerRegistry(I)Lnet/minecraft/world/level/gameevent/GameEventListenerRegistry;",
            at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/level/chunk/LevelChunk;gameEventListenerRegistrySections:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;")
    )
    private void initializeCollection(CallbackInfoReturnable<GameEventListenerRegistry> cir) {
        if (this.gameEventListenerRegistrySections == null) {
            this.setGameEventListenerRegistrySections(new Int2ObjectOpenHashMap<>(4));
        }
    }

    @Unique
    public void setGameEventListenerRegistrySections(Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections) {
        GameEventDispatcherStorage storage = ((LithiumData) this.getLevel()).lithium$getData().gameEventDispatchers();
        storage.replace(this.getPos().toLong(), gameEventListenerRegistrySections);
        this.gameEventListenerRegistrySections = gameEventListenerRegistrySections;
    }
}
