package rbasamoyai.ogden.ammunition;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.entities.AmmunitionPropertiesEntity;
import rbasamoyai.ogden.network.OgdenNetwork;
import rbasamoyai.ogden.network.StandardPacket;

public class AmmunitionPropertiesHandler {

    private static final Map<Item, AmmunitionProperties> PROPERTIES = new Reference2ObjectOpenHashMap<>();
    private static final Map<Item, AmmunitionPropertiesSerializer<?>> SERIALIZERS = new Reference2ReferenceOpenHashMap<>();

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final ReloadListener INSTANCE = new ReloadListener();

        ReloadListener() { super(GSON, "ammunition_properties"); }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
            PROPERTIES.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                JsonElement el = entry.getValue();
                if (!el.isJsonObject()) continue;
                ResourceLocation loc = entry.getKey();
                try {
                    Item item = OgdenRegistryUtils.getOptionalItemFromId(loc).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown item '" + loc + "'");
                    });
                    AmmunitionPropertiesSerializer<?> ser = SERIALIZERS.get(item);
                    if (ser == null)
                        throw new JsonSyntaxException("No configuration for item '" + loc + "' present");
                    PROPERTIES.put(item, ser.fromJson(loc, el.getAsJsonObject()));
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
        }
    }

    public static <P extends AmmunitionProperties, S extends AmmunitionPropertiesSerializer<P>> S registerSerializer(
            EntityType<? extends AmmunitionPropertiesEntity<P>> type, Item item, S ser) {
        if (SERIALIZERS.containsKey(item))
            throw new IllegalStateException("Serializer for item " + OgdenRegistryUtils.getItemId(item) + " already registered");
        SERIALIZERS.put(item, ser);
        return ser;
    }

    @Nullable public static AmmunitionProperties getProperties(ItemStack stack) { return getProperties(stack.getItem()); }

    @Nullable public static AmmunitionProperties getProperties (Item item) { return PROPERTIES.get(item); }

    public static void writeBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(PROPERTIES.size());
        for (Map.Entry<Item, AmmunitionProperties> entry : PROPERTIES.entrySet()) {
            buf.writeResourceLocation(OgdenRegistryUtils.getItemId(entry.getKey()));
            toNetworkCasted(buf, entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AmmunitionProperties> void toNetworkCasted(FriendlyByteBuf buf, Item item, T properties) {
        AmmunitionPropertiesSerializer<T> ser = (AmmunitionPropertiesSerializer<T>) SERIALIZERS.get(item);
        ser.toNetwork(properties, buf);
    }

    public static void readBuf(FriendlyByteBuf buf) {
        int sz = buf.readVarInt();
        for (int i = 0; i < sz; ++i) {
            ResourceLocation loc = buf.readResourceLocation();
            Item item = OgdenRegistryUtils.getItemFromId(loc);
            PROPERTIES.put(item, SERIALIZERS.get(item).fromNetwork(loc, buf));
        }
    }

    public static void syncToAll(MinecraftServer server) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundSyncAmmunitionPropertiesPacket());
    }

    public static void syncToPlayer(ServerPlayer player) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSyncAmmunitionPropertiesPacket());
    }

    public record ClientboundSyncAmmunitionPropertiesPacket(@Nullable FriendlyByteBuf buf) implements StandardPacket {
        public ClientboundSyncAmmunitionPropertiesPacket() { this(null); }

        public static ClientboundSyncAmmunitionPropertiesPacket copyOf(FriendlyByteBuf buf) {
            return new ClientboundSyncAmmunitionPropertiesPacket(new FriendlyByteBuf(buf.copy()));
        }

        @Override public void encode(FriendlyByteBuf buf) { writeBuf(buf); }

        @Override
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (this.buf != null) readBuf(this.buf);
            });
            ctx.get().setPacketHandled(true);
        }
    }

}
