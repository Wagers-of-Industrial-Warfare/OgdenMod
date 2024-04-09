package rbasamoyai.ogden.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record ClientboundCheckChannelVersionPacket(String serverVersion) implements StandardPacket {

	public ClientboundCheckChannelVersionPacket(FriendlyByteBuf buf) {
		this(buf.readUtf());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(this.serverVersion);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OgdenClientboundPacketHandlers.checkVersion(this));
        });
        ctx.get().setPacketHandled(true);
	}

}
