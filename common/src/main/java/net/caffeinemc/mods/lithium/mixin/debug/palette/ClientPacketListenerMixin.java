package net.caffeinemc.mods.lithium.mixin.debug.palette;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @WrapMethod(
            method = "updateLevelChunk(IILnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;)V"
    )
    private void addExceptionInfo(int i, int j, ClientboundLevelChunkPacketData clientboundLevelChunkPacketData, Operation<Void> original) {
        try {
            original.call(i, j, clientboundLevelChunkPacketData);
        } catch (IllegalStateException e) {
            String message = "Exception occurred while receiving data for chunk at " + i + ", " + j + ".\n" +
                    "**The following may include sensitive data, e.g. text that is written with blocks or built \n" +
                    "structures. Make sure the chunk with chunk coordinates " + i + ", " + j + " does not contain block\n" +
                    "or biome structures (e.g. your non-pseudonym name written with blocks) that you do not want\n" +
                    "published. This does not include block entities or items.**\n" +
                    "Possible sensitive chunk biome and blockstate data: " + Arrays.toString(((ClientBoundLevelChunkPacketDataAccessor) clientboundLevelChunkPacketData).getBuffer());
            throw new IllegalStateException(message, e);
        }
    }
}
