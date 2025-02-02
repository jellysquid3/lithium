package net.caffeinemc.mods.lithium.mixin.debug.palette;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.chunk.MissingPaletteEntryException;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PalettedContainer.class)
public class PalettedContainerMixin<T> {

    @Shadow
    private volatile PalettedContainer.Data<T> data;

    @Inject(
            method = "read", at = @At("RETURN")
    )
    private void checkConsistency(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        BitStorage storage = this.data.storage();
        Palette<T> palette = this.data.palette();
        int i = -1;
        int index = -1;
        try {
            for (i = 0; i < storage.getSize(); i++) {
                index = storage.get(i);
                T t = palette.valueFor(index);
                //noinspection ConstantValue
                if (t == null) {
                    throw new MissingPaletteEntryException(index);
                }
            }
        } catch (Exception e) {
            String builder = "Received invalid paletted container data!\n" +
                    "Entry at index " + i + " has palette index " + index + ".\n" +
                    "Palette: " + palette + " Size: " + palette.getSize() + "\n";
            throw new IllegalStateException(builder, e);
        }
    }
}
