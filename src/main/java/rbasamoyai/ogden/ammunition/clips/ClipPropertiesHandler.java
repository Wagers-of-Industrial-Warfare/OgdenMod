package rbasamoyai.ogden.ammunition.clips;

import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.ogden.base.OgdenItemPredicate;
import rbasamoyai.ogden.base.OgdenJsonResourceReloadListener;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.network.OgdenNetwork;
import rbasamoyai.ogden.network.StandardPacket;

public class ClipPropertiesHandler {

    private static final Map<Item, Integer> CLIP_CAPACITY = new Reference2IntOpenHashMap<>();
    private static final Map<Item, OgdenItemPredicate> ACCEPTED_AMMO = new Reference2ObjectOpenHashMap<>();

    public static class CapacityReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final CapacityReloadListener INSTANCE = new CapacityReloadListener();

        CapacityReloadListener() { super(GSON, "clip_properties/capacity"); }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
            CLIP_CAPACITY.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                try {
                    ResourceLocation loc = entry.getKey();
                    JsonElement el = entry.getValue();
                    if (!el.isJsonObject())
                        throw new JsonParseException("Expected clip properties for '" + loc + "' to be a JSON object");
                    JsonObject obj = el.getAsJsonObject();
                    Item item = OgdenRegistryUtils.getOptionalItemFromId(loc).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown item '" + loc + "'");
                    });
                    CLIP_CAPACITY.put(item, GsonHelper.getAsInt(obj, "capacity"));
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
        }
    }

    public static int getAmmoCapacity(ItemStack itemStack) { return getAmmoCapacity(itemStack.getItem()); }

    public static int getAmmoCapacity(Item item) { return CLIP_CAPACITY.getOrDefault(item, -1); }

    public static class AcceptedAmmoReloadListener extends OgdenJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        private static final Logger LOGGER = LogUtils.getLogger();
        public static final AcceptedAmmoReloadListener INSTANCE = new AcceptedAmmoReloadListener();

        AcceptedAmmoReloadListener() { super(GSON, "clip_properties/accepted_ammunition"); }

        @Override
        protected void apply(Multimap<ResourceLocation, JsonElement> multimap, ResourceManager manager, ProfilerFiller profiler) {
            ACCEPTED_AMMO.clear();

            for (Map.Entry<ResourceLocation, JsonElement> entry : multimap.entries()) {
                try {
                    ResourceLocation loc = entry.getKey();
                    JsonElement el = entry.getValue();
                    if (!el.isJsonObject())
                        throw new JsonParseException("Expected clip ammunition properties for '" + loc + "' to be a JSON object");
                    JsonObject obj = el.getAsJsonObject();

                    Item item = OgdenRegistryUtils.getOptionalItemFromId(loc).orElseThrow(() -> {
                        return new JsonSyntaxException("Unknown item '" + loc + "'");
                    });
                    JsonArray arr = GsonHelper.getAsJsonArray(obj, "ammunition");
                    OgdenItemPredicate pred = OgdenItemPredicate.fromJson(arr);
                    if (GsonHelper.getAsBoolean(obj, "replace", false) || !ACCEPTED_AMMO.containsKey(item)) {
                        ACCEPTED_AMMO.put(item, pred);
                    } else {
                        ACCEPTED_AMMO.put(item, ACCEPTED_AMMO.get(item).merge(pred));
                    }
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
        }
    }

    public static OgdenItemPredicate getAmmoPredicate(ItemStack itemStack) { return getAmmoPredicate(itemStack.getItem()); }
    public static OgdenItemPredicate getAmmoPredicate(Item item) { return ACCEPTED_AMMO.getOrDefault(item, OgdenItemPredicate.EMPTY); }

    public static void loadTags() {
        for (OgdenItemPredicate pred : ACCEPTED_AMMO.values())
            pred.loadTags();
    }

    public static void writeBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(CLIP_CAPACITY.size());
        for (Map.Entry<Item, Integer> entry : CLIP_CAPACITY.entrySet()) {
            buf.writeResourceLocation(OgdenRegistryUtils.getItemId(entry.getKey()))
                .writeVarInt(entry.getValue());
        }
        buf.writeVarInt(ACCEPTED_AMMO.size());
        for (Map.Entry<Item, OgdenItemPredicate> entry : ACCEPTED_AMMO.entrySet()) {
            buf.writeResourceLocation(OgdenRegistryUtils.getItemId(entry.getKey()));
            entry.getValue().toNetwork(buf);
        }
    }

    public static void readBuf(FriendlyByteBuf buf) {
        CLIP_CAPACITY.clear();
        int clipSz = buf.readVarInt();
        for (int i = 0; i < clipSz; ++i) {
            ResourceLocation loc = buf.readResourceLocation();
            Item item = OgdenRegistryUtils.getItemFromId(loc);
            CLIP_CAPACITY.put(item, buf.readVarInt());
        }
        ACCEPTED_AMMO.clear();
        int ammoSz = buf.readVarInt();
        for (int i = 0; i < ammoSz; ++i) {
            ResourceLocation loc = buf.readResourceLocation();
            Item item = OgdenRegistryUtils.getItemFromId(loc);
            ACCEPTED_AMMO.put(item, OgdenItemPredicate.fromNetwork(buf));
        }
    }

    public static void syncToAll(MinecraftServer server) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundSyncClipPropertiesPacket());
    }

    public static void syncToPlayer(ServerPlayer player) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSyncClipPropertiesPacket());
    }

    public record ClientboundSyncClipPropertiesPacket(@Nullable FriendlyByteBuf buf) implements StandardPacket {
        public ClientboundSyncClipPropertiesPacket() { this(null); }

        public static ClientboundSyncClipPropertiesPacket copyOf(FriendlyByteBuf buf) {
            return new ClientboundSyncClipPropertiesPacket(new FriendlyByteBuf(buf.copy()));
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
