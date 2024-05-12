package rbasamoyai.ogden.ammunition.clips;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.base.OgdenItemPredicate;

public interface IClipItem {

    IClipItem NO_OVERRIDES = new IClipItem() {};

    default List<ItemStack> getStoredAmmo(ItemStack itemStack) {
        return getStoredAmmoBase(itemStack);
    }

    default int addAmmo(ItemStack clipItemStack, ItemStack ammoItemStack, boolean simulate) {
        return addAmmoBase(clipItemStack, ammoItemStack, simulate);
    }

    default int addAmmo(ItemStack clipItemStack, List<ItemStack> ammoItemStacks, boolean simulate) {
        return addAmmoBase(clipItemStack, ammoItemStacks, simulate);
    }

    default int getAmmoCapacity(ItemStack itemStack) {
        return getAmmoCapacityBase(itemStack);
    }

    default OgdenItemPredicate getAmmoPredicate(ItemStack itemStack) {
        return getAmmoPredicateBase(itemStack);
    }

    static List<ItemStack> getStoredAmmoBase(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        if (!tag.contains("StoredAmmo", Tag.TAG_LIST))
            tag.put("StoredAmmo", new ListTag());
        ListTag storedAmmoTag = tag.getList("StoredAmmo", Tag.TAG_COMPOUND);
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < storedAmmoTag.size(); ++i)
            list.add(ItemStack.of(storedAmmoTag.getCompound(i)));
        return list;
    }

    static int addAmmoBase(ItemStack clipItemStack, ItemStack ammoItemStack, boolean simulate) {
        OgdenItemPredicate ammoPredicate = clipItemStack.getItem() instanceof IClipItem iClipItem
            ? iClipItem.getAmmoPredicate(clipItemStack)
            : getAmmoPredicateBase(clipItemStack);
        if (!ammoPredicate.test(ammoItemStack))
            return 0;
        CompoundTag tag = clipItemStack.getOrCreateTag();
        if (!tag.contains("StoredAmmo", Tag.TAG_LIST))
            tag.put("StoredAmmo", new ListTag());
        ListTag storedAmmoTag = tag.getList("StoredAmmo", Tag.TAG_COMPOUND);
        int ammoCapacity = asClipItem(clipItemStack).getAmmoCapacity(clipItemStack);
        int tryInsertCount = Math.min(ammoItemStack.getCount(), ammoCapacity - storedAmmoTag.size());
        if (tryInsertCount < 1)
            return 0;
        if (!simulate) {
            ItemStack split = ammoItemStack.split(tryInsertCount);
            split.setCount(1);
            for (int i = 0 ; i < tryInsertCount; ++i)
                storedAmmoTag.add(split.save(new CompoundTag()));
        }
        return tryInsertCount;
    }

    static int addAmmoBase(ItemStack clipItemStack, List<ItemStack> ammoItemStacks, boolean simulate) {
        CompoundTag tag = clipItemStack.getOrCreateTag();
        if (!tag.contains("StoredAmmo", Tag.TAG_LIST))
            tag.put("StoredAmmo", new ListTag());
        ListTag storedAmmoTag = tag.getList("StoredAmmo", Tag.TAG_COMPOUND);
        IClipItem clip = asClipItem(clipItemStack);
        int ammoCapacity = clip.getAmmoCapacity(clipItemStack);
        int canInsertCount = ammoCapacity - storedAmmoTag.size();
        if (canInsertCount < 1)
            return 0;
        OgdenItemPredicate ammoPredicate = clip.getAmmoPredicate(clipItemStack);
        int counter = 0;
        for (ItemStack ammoItemStack : ammoItemStacks) {
            if (!ammoPredicate.test(ammoItemStack))
                return counter;
            int canInsert = Math.min(ammoItemStack.getCount(), canInsertCount - counter);
            if (canInsert < 1)
                return counter;
            counter += canInsert;
            if (!simulate) {
                ItemStack split = ammoItemStack.split(canInsert);
                split.setCount(1);
                for (int i = 0 ; i < canInsert; ++i)
                    storedAmmoTag.add(split.save(new CompoundTag()));
            }
        }
        return counter;
    }

    static int getAmmoCapacityBase(ItemStack itemStack) {
        return ClipPropertiesHandler.getAmmoCapacity(itemStack);
    }

    static OgdenItemPredicate getAmmoPredicateBase(ItemStack itemStack) {
        return ClipPropertiesHandler.getAmmoPredicate(itemStack);
    }

    static IClipItem asClipItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof IClipItem clip ? clip : NO_OVERRIDES;
    }

}
