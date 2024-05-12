package rbasamoyai.ogden.firearms.config;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.ammunition.clips.IClipItem;
import rbasamoyai.ogden.base.OgdenItemPredicate;

public class FirearmAmmoPredicate implements Predicate<ItemStack> {

    public static final FirearmAmmoPredicate EMPTY = new FirearmAmmoPredicate(false, OgdenItemPredicate.EMPTY, OgdenItemPredicate.EMPTY);

    @Nullable private final Boolean needsClip;
    private final OgdenItemPredicate ammoPredicate;
    private final OgdenItemPredicate clipPredicate;

    public FirearmAmmoPredicate(@Nullable Boolean needsClip, OgdenItemPredicate ammoPredicate, OgdenItemPredicate clipPredicate) {
        this.needsClip = needsClip;
        this.ammoPredicate = ammoPredicate;
        this.clipPredicate = clipPredicate;
    }

    public static FirearmAmmoPredicate fromJson(JsonObject obj) {
        Boolean needsClip = GsonHelper.isBooleanValue(obj, "needs_clip") ? GsonHelper.getAsBoolean(obj, "needs_clip") : null;

        JsonArray ammoArr = GsonHelper.getAsJsonArray(obj, "ammunition");
        OgdenItemPredicate ammoPredicate = OgdenItemPredicate.fromJson(ammoArr);
        OgdenItemPredicate clipPredicate;
        if (GsonHelper.isArrayNode(obj, "clips")) {
            JsonArray clipArr = GsonHelper.getAsJsonArray(obj, "clips");
            clipPredicate = OgdenItemPredicate.fromJson(clipArr);
        } else {
            clipPredicate = OgdenItemPredicate.EMPTY;
        }
        return new FirearmAmmoPredicate(needsClip, ammoPredicate, clipPredicate);
    }

    public FirearmAmmoPredicate merge(FirearmAmmoPredicate other) {
        Boolean newNeedsClip = other.needsClip == null ? this.needsClip : other.needsClip;
        OgdenItemPredicate newAmmo = this.ammoPredicate.merge(other.ammoPredicate);
        OgdenItemPredicate newClip = this.clipPredicate.merge(other.clipPredicate);
        return new FirearmAmmoPredicate(newNeedsClip, newAmmo, newClip);
    }

    public void loadTags() {
        this.ammoPredicate.loadTags();
        this.clipPredicate.loadTags();
    }

    public ItemStack getCreativeItemStack() {
        ItemStack ammoStack = this.ammoPredicate.getModalItem();
        if (ammoStack.isEmpty())
            return ItemStack.EMPTY;
        if (!this.needsClip())
            return ammoStack;
        ItemStack clipStack = this.clipPredicate.getModalItem();
        if (clipStack.isEmpty())
            return ItemStack.EMPTY;
        IClipItem clipItem = IClipItem.asClipItem(clipStack);
        int capacity = clipItem.getAmmoCapacity(clipStack);
        if (capacity < 1)
            return ItemStack.EMPTY;
        for (int i = 0; i < capacity; ++i)
            clipItem.addAmmo(clipStack, ammoStack.copy(), false);
        return clipStack;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(this.needsClip());
        this.ammoPredicate.toNetwork(buf);
        this.clipPredicate.toNetwork(buf);
    }

    public static FirearmAmmoPredicate fromNetwork(FriendlyByteBuf buf) {
        Boolean needsClip = buf.readBoolean();
        OgdenItemPredicate ammoPredicate = OgdenItemPredicate.fromNetwork(buf);
        OgdenItemPredicate clipPredicate = OgdenItemPredicate.fromNetwork(buf);
        return new FirearmAmmoPredicate(needsClip, ammoPredicate, clipPredicate);
    }

    public boolean needsClip() { return this.needsClip != null && this.needsClip; }

    @Override
    public boolean test(ItemStack itemStack) {
        return !this.needsClip() && this.testAmmo(itemStack) || this.testClip(itemStack);
    }

    public boolean testAmmo(ItemStack itemStack) { return this.ammoPredicate.test(itemStack); }

    public boolean testClip(ItemStack itemStack) {
        if (!this.clipPredicate.test(itemStack))
            return false;
        List<ItemStack> clipStacks = IClipItem.asClipItem(itemStack).getStoredAmmo(itemStack);
        if (clipStacks.isEmpty())
            return false;
        for (ItemStack ammoStack : clipStacks) {
            if (!this.testAmmo(ammoStack))
                return false;
        }
        return true;
    }

    public int countAmmo(ItemStack itemStack) {
        if (this.testAmmo(itemStack))
            return itemStack.getCount();
        if (!this.clipPredicate.test(itemStack))
            return 0;
        int count = 0;
        List<ItemStack> clipStacks = IClipItem.asClipItem(itemStack).getStoredAmmo(itemStack);
        if (clipStacks.isEmpty())
            return 0;
        for (ItemStack ammoStack : clipStacks) {
            if (this.testAmmo(ammoStack)) {
                count += ammoStack.getCount();
            } else {
                return 0; // Clip cannot be used if cannot insert, and thus not available
            }
        }
        return count;
    }

}
