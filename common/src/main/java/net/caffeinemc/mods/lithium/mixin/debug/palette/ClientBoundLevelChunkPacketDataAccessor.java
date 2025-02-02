package net.caffeinemc.mods.lithium.mixin.debug.palette;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundLevelChunkPacketData.class)
public interface ClientBoundLevelChunkPacketDataAccessor {

    @Accessor("buffer")
    byte[] getBuffer();

}
