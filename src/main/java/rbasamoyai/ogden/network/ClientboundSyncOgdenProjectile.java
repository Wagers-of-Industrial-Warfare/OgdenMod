package rbasamoyai.ogden.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import rbasamoyai.ogden.entities.OgdenProjectile;

public record ClientboundSyncOgdenProjectile(int id, CompoundTag syncedData) implements StandardPacket  {

    public ClientboundSyncOgdenProjectile(OgdenProjectile<?> projectile) {
        this(projectile.getId(), writeSyncData(projectile));
    }

    public ClientboundSyncOgdenProjectile(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.id).writeNbt(this.syncedData);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OgdenClientboundPacketHandlers.syncOgdenProjectileData(this));
        });
        ctx.get().setPacketHandled(true);
    }

    private static CompoundTag writeSyncData(OgdenProjectile<?> projectile) {
        CompoundTag tag = new CompoundTag();
        projectile.writeProjectileSyncData(tag);
        return tag;
    }

}
