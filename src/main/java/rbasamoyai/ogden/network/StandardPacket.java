package rbasamoyai.ogden.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface StandardPacket {

    void encode(FriendlyByteBuf buf);
    void handle(Supplier<NetworkEvent.Context> ctx);

}
