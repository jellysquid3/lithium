package net.caffeinemc.mods.lithium.common.util;

import net.minecraft.world.entity.EquipmentSlot;

/**
 * Pre-initialized constants to avoid unnecessary allocations.
 */
public final class EquipmentSlotConstants {
    public static final EquipmentSlot[] ALL = EquipmentSlot.values();

    private EquipmentSlotConstants() {
    }
}
