package net.caffeinemc.mods.lithium.mixin.alloc.entity_iteration;

import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ClassInstanceMultiMap.class)
public interface ClassInstanceMultiMapAccessor<T> {

    @Accessor("allInstances")
    List<T> getAllInstances();

}
