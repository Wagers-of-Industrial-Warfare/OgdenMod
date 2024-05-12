package rbasamoyai.ogden.base;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class OgdenRegistryUtils {

    public static ResourceLocation getItemId(ItemStack stack) { return getItemId(stack.getItem()); }

    public static ResourceLocation getItemId(Item item) { return Registry.ITEM.getKey(item); }

    public static Item getItemFromId(ResourceLocation loc) { return Registry.ITEM.get(loc); }
    public static Optional<Item> getOptionalItemFromId(ResourceLocation loc) { return Registry.ITEM.getOptional(loc); }

    public static Iterable<Holder<Item>> getItemsOfTag(TagKey<Item> tag) { return Registry.ITEM.getTagOrEmpty(tag); }

    public static TagKey<Item> makeItemTag(ResourceLocation loc) { return TagKey.create(Registry.ITEM_REGISTRY, loc); }

    private OgdenRegistryUtils() {
    }

}
