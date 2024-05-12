package rbasamoyai.ogden.firearms.config;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.ogden.base.OgdenJsonResourceReloadListener;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.network.OgdenNetwork;
import rbasamoyai.ogden.network.StandardPacket;

public class FirearmAcceptedAmmunitionHandler {

    private static final Map<Item, FirearmAmmoPredicate> PREDICATES = new Reference2ObjectOpenHashMap<>();

    public static class ReloadListener extends OgdenJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final ReloadListener INSTANCE = new ReloadListener();

        ReloadListener() { super(GSON, "firearm_properties/ammunition"); }

        @Override
        protected void apply(Multimap<ResourceLocation, JsonElement> multimap, ResourceManager manager, ProfilerFiller profiler) {
            PREDICATES.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : multimap.entries()) {
                JsonElement el = entry.getValue();
                if (!el.isJsonObject()) continue;
                JsonObject obj = el.getAsJsonObject();
                ResourceLocation loc = entry.getKey();
                try {
                    Item item = OgdenRegistryUtils.getOptionalItemFromId(loc).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown item '" + loc + "'");
                    });
                    FirearmAmmoPredicate pred = FirearmAmmoPredicate.fromJson(obj);
                    if (GsonHelper.getAsBoolean(obj, "replace", false) || !PREDICATES.containsKey(item)) {
                        PREDICATES.put(item, pred);
                    } else {
                        PREDICATES.put(item, PREDICATES.get(item).merge(pred));
                    }
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
        }
    }

    public static void loadTags() {
        for (FirearmAmmoPredicate col : PREDICATES.values())
            col.loadTags();
    }

    public static FirearmAmmoPredicate getAmmoPredicate(ItemStack stack) { return getAmmoPredicate(stack.getItem()); }
    public static FirearmAmmoPredicate getAmmoPredicate(Item item) { return PREDICATES.getOrDefault(item, FirearmAmmoPredicate.EMPTY); }

    public static void syncToAll(MinecraftServer server) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundSyncFirearmAcceptedAmmunitionPacket());
    }

    public static void syncToPlayer(ServerPlayer player) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSyncFirearmAcceptedAmmunitionPacket());
    }

    public static void writeBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(PREDICATES.size());
        for (Map.Entry<Item, FirearmAmmoPredicate> entry : PREDICATES.entrySet()) {
            buf.writeResourceLocation(OgdenRegistryUtils.getItemId(entry.getKey()));
            entry.getValue().toNetwork(buf);
        }
    }

    public static void readBuf(FriendlyByteBuf buf) {
        PREDICATES.clear();
        int sz = buf.readVarInt();
        for (int i = 0; i < sz; ++i) {
            PREDICATES.put(OgdenRegistryUtils.getItemFromId(buf.readResourceLocation()), FirearmAmmoPredicate.fromNetwork(buf));
        }
    }

    public record ClientboundSyncFirearmAcceptedAmmunitionPacket(@Nullable FriendlyByteBuf buf) implements StandardPacket {
        public ClientboundSyncFirearmAcceptedAmmunitionPacket() { this(null); }

        public static ClientboundSyncFirearmAcceptedAmmunitionPacket copyOf(FriendlyByteBuf buf) {
            return new ClientboundSyncFirearmAcceptedAmmunitionPacket(new FriendlyByteBuf(buf.copy()));
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
