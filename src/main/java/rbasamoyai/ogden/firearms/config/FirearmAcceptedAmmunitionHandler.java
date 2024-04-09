package rbasamoyai.ogden.firearms.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
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

    private static final Map<Item, AmmunitionCollection> PREDICATES = new Reference2ObjectOpenHashMap<>();

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
                    AmmunitionCollection col = AmmunitionCollection.fromJson(obj);
                    if (GsonHelper.getAsBoolean(obj, "replace", false) || !PREDICATES.containsKey(item)) {
                        PREDICATES.put(item, col);
                    } else {
                        AmmunitionCollection existing = PREDICATES.get(item);
                        existing.items.addAll(col.items);
                        existing.tags.addAll(col.tags);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.toString());
                }
            }
        }
    }

    private record AmmunitionCollection(Set<Item> items, Set<TagKey<Item>> tags) implements Predicate<ItemStack> {
        public static AmmunitionCollection fromJson(JsonObject obj) {
            Set<Item> items = new ReferenceOpenHashSet<>();
            Set<TagKey<Item>> tags = new HashSet<>();
            JsonElement el = obj.get("values");
            if (el.isJsonArray()) {
                JsonArray arr = el.getAsJsonArray();
                for (JsonElement el1 : arr) {
                    // Adapted from Tag$Builder#parseEntry
                    String s;
                    boolean flag;
                    if (el1.isJsonObject()) {
                        JsonObject obj1 = el1.getAsJsonObject();
                        s = GsonHelper.getAsString(obj1, "id");
                        flag = GsonHelper.getAsBoolean(obj1, "required", true);
                    } else {
                        s = GsonHelper.convertToString(el1, "id");
                        flag = true;
                    }
                    if (s.startsWith("#")) {
                        tags.add(OgdenRegistryUtils.makeItemTag(new ResourceLocation(s.substring(1))));
                    } else {
                        ResourceLocation loc = new ResourceLocation(s);
                        Optional<Item> op = OgdenRegistryUtils.getOptionalItemFromId(loc);
                        if (flag && op.isEmpty())
                            throw new JsonParseException("Unknown item '" + loc + "'");
                        op.ifPresent(items::add);
                    }
                }
            }
            return new AmmunitionCollection(items, tags);
        }

        // Technically not necessary to write tags, but may be useful somehow for information in the future --ritchie

        public void toNetwork(FriendlyByteBuf buf) {
            buf.writeVarInt(this.items.size());
            for (Item item : this.items) {
                buf.writeResourceLocation(OgdenRegistryUtils.getItemId(item));
            }
            buf.writeVarInt(this.tags.size());
            for (TagKey<Item> tag : this.tags) {
                buf.writeResourceLocation(tag.location());
            }
        }

        public static AmmunitionCollection fromNetwork(FriendlyByteBuf buf) {
            Set<Item> items = new ReferenceOpenHashSet<>();
            int itemsSz = buf.readVarInt();
            for (int i = 0; i < itemsSz; ++i) {
                items.add(OgdenRegistryUtils.getItemFromId(buf.readResourceLocation()));
            }
            Set<TagKey<Item>> tags = new HashSet<>();
            int tagsSz = buf.readVarInt();
            for (int i = 0; i < tagsSz; ++i) {
                tags.add(OgdenRegistryUtils.makeItemTag(buf.readResourceLocation()));
            }
            return new AmmunitionCollection(items, tags);
        }

        @Override public boolean test(ItemStack itemStack) { return this.items.contains(itemStack.getItem()); }
    }

    public static void loadTags() {
        for (AmmunitionCollection col : PREDICATES.values()) {
            for (TagKey<Item> tag : col.tags) {
                for (Holder<Item> holder : OgdenRegistryUtils.getItemsOfTag(tag)) {
                    col.items.add(holder.value());
                }
            }
        }
    }

    public static Predicate<ItemStack> getPredicate(ItemStack stack) { return getPredicate(stack.getItem()); }
    public static Predicate<ItemStack> getPredicate(Item item) { return PREDICATES.containsKey(item) ? PREDICATES.get(item) : s -> false; }

    public static void syncToAll(MinecraftServer server) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundSyncFirearmAcceptedAmmunitionPacket());
    }

    public static void syncToPlayer(ServerPlayer player) {
        OgdenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ClientboundSyncFirearmAcceptedAmmunitionPacket());
    }

    public static void writeBuf(FriendlyByteBuf buf) {
        buf.writeVarInt(PREDICATES.size());
        for (Map.Entry<Item, AmmunitionCollection> entry : PREDICATES.entrySet()) {
            buf.writeResourceLocation(OgdenRegistryUtils.getItemId(entry.getKey()));
            entry.getValue().toNetwork(buf);
        }
    }

    public static void readBuf(FriendlyByteBuf buf) {
        PREDICATES.clear();
        int sz = buf.readVarInt();
        for (int i = 0; i < sz; ++i) {
            PREDICATES.put(OgdenRegistryUtils.getItemFromId(buf.readResourceLocation()), AmmunitionCollection.fromNetwork(buf));
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
