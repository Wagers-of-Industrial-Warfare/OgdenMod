package rbasamoyai.ogden.network;

import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler.ClientboundSyncAmmunitionPropertiesPacket;

public class OgdenNetwork {

    public static final String VERSION = "0.0.1";

    public static final SimpleChannel INSTANCE = construct();

    public static SimpleChannel construct() {
        SimpleChannel channel = NetworkRegistry.ChannelBuilder
            .named(OgdenMod.resource("network"))
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .networkProtocolVersion(() -> VERSION)
            .simpleChannel();

        int id = 0;

        addStandardPacket(channel, id++, ClientboundCheckChannelVersionPacket.class, ClientboundCheckChannelVersionPacket::new);
        addStandardPacket(channel, id++, ClientboundSyncOgdenProjectile.class, ClientboundSyncOgdenProjectile::new);
        addStandardPacket(channel, id++, ClientboundSyncAmmunitionPropertiesPacket.class, ClientboundSyncAmmunitionPropertiesPacket::copyOf);

        return channel;
    }

    public static <MSG extends StandardPacket> void addStandardPacket(SimpleChannel channel, int id, Class<MSG> clazz, Function<FriendlyByteBuf, MSG> decoder) {
        channel.messageBuilder(clazz, id)
            .encoder(StandardPacket::encode)
            .decoder(decoder)
            .consumer(StandardPacket::handle)
            .add();
    }

    public static void init() {
    }

}
