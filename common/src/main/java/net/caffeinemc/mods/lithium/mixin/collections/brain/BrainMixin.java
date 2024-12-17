package net.caffeinemc.mods.lithium.mixin.collections.brain;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.function.Supplier;

@Mixin(Brain.class)
public class BrainMixin {

    @Mutable
    @Shadow
    @Final
    private Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories;

    @Mutable
    @Shadow
    @Final
    private Map<?, ?> sensors;

    @Shadow
    @Final
    @Mutable
    private Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements;

    @Inject(
            method = "<init>(Ljava/util/Collection;Ljava/util/Collection;Lcom/google/common/collect/ImmutableList;Ljava/util/function/Supplier;)V",
            at = @At("RETURN")
    )
    private void reinitializeBrainCollections(Collection<?> memories, Collection<?> sensors, ImmutableList<?> memoryEntries, Supplier<?> codecSupplier, CallbackInfo ci) {
        this.memories = new Reference2ObjectOpenHashMap<>(this.memories);
        this.sensors = new Reference2ReferenceLinkedOpenHashMap<>(this.sensors);
        this.activityRequirements = new Object2ObjectOpenHashMap<>(this.activityRequirements);
    }

    @Redirect(
            method = "forgetOutdatedMemories", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;")
    )
    private <K,V> Set<Map.Entry<K, V>> redirectIterator(Map<K, V> instance) {
        return null;
    }
    @Redirect(
            method = "forgetOutdatedMemories", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;")
    )
    private Iterator<? extends Map.Entry<?, ?>> redirectIterator(Set<Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>>> set) {
        if (this.memories instanceof Reference2ObjectOpenHashMap<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> fastMap) {
            return fastMap.reference2ObjectEntrySet().fastIterator();
        }
        return this.memories.entrySet().iterator();
    }

}
