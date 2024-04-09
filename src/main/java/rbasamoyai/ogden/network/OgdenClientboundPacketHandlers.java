package rbasamoyai.ogden.network;

import net.minecraft.client.Minecraft;
import rbasamoyai.ogden.base.Components;
import rbasamoyai.ogden.entities.OgdenProjectile;

public class OgdenClientboundPacketHandlers {

    public static void checkVersion(ClientboundCheckChannelVersionPacket pkt) {
        if (OgdenNetwork.VERSION.equals(pkt.serverVersion())) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null)
            mc.getConnection().onDisconnect(Components.literal("Ogden on the client uses a different network format than the server.")
                .append(" Please use a matching format."));
    }

    public static void syncOgdenProjectileData(ClientboundSyncOgdenProjectile pkt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(pkt.id()) instanceof OgdenProjectile<?> proj)
            proj.readProjectileSyncData(pkt.syncedData());
    }

}
