package rbasamoyai.ogden.base;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OgdenItemPredicate implements Predicate<ItemStack> {

    public static final OgdenItemPredicate EMPTY = new OgdenItemPredicate(Set.of(), Set.of());

    private final Set<Item> items;
    private final Set<TagKey<Item>> tags;
    private ItemStack modalItem = null;

    public OgdenItemPredicate(Set<Item> items, Set<TagKey<Item>> tags) {
        this.items = items;
        this.tags = tags;
    }

    public static OgdenItemPredicate fromJson(JsonArray arr) {
        // Adapted from Tag$Builder#parseEntry
        Set<Item> items = new ReferenceLinkedOpenHashSet<>();
        Set<TagKey<Item>> tags = new HashSet<>();
        for (JsonElement el1 : arr) {
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
        return new OgdenItemPredicate(items, tags);
    }

    public OgdenItemPredicate merge(OgdenItemPredicate other) {
        Set<Item> newItems = new ReferenceOpenHashSet<>();
        newItems.addAll(this.items);
        newItems.addAll(other.items);
        Set<TagKey<Item>> newTags = new HashSet<>(this.tags);
        newTags.addAll(other.tags);
        return new OgdenItemPredicate(newItems, newTags);
    }

    public void loadTags() {
        for (TagKey<Item> tag : this.tags) {
            for (Holder<Item> holder : OgdenRegistryUtils.getItemsOfTag(tag)) {
                this.items.add(holder.value());
            }
        }
    }

    public ItemStack getModalItem() {
        if (this.modalItem == null) {
            Iterator<Item> iter = this.items.iterator();
            this.modalItem = iter.hasNext() ? new ItemStack(iter.next()) : ItemStack.EMPTY;
        }
        return this.modalItem;
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

    public static OgdenItemPredicate fromNetwork(FriendlyByteBuf buf) {
        Set<Item> items = new ReferenceOpenHashSet<>();
        int ammoItemsSz = buf.readVarInt();
        for (int i = 0; i < ammoItemsSz; ++i) {
            items.add(OgdenRegistryUtils.getItemFromId(buf.readResourceLocation()));
        }
        Set<TagKey<Item>> tags = new HashSet<>();
        int tagsSz = buf.readVarInt();
        for (int i = 0; i < tagsSz; ++i) {
            tags.add(OgdenRegistryUtils.makeItemTag(buf.readResourceLocation()));
        }
        return new OgdenItemPredicate(items, tags);
    }

    @Override public boolean test(ItemStack itemStack) { return this.items.contains(itemStack.getItem()); }

}
