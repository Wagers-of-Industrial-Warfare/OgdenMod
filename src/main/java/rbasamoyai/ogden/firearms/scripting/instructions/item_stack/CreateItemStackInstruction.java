package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CreateItemStackInstruction implements ScriptInstruction {

    private final ScriptValueSupplier item;
    @Nullable private final ScriptValueSupplier count;
    @Nullable private final ScriptValueSupplier damage;
    @Nullable private final ScriptValueSupplier tag;

    public CreateItemStackInstruction(ScriptValueSupplier item, @Nullable ScriptValueSupplier count, @Nullable ScriptValueSupplier damage,
                                      @Nullable ScriptValueSupplier tag) {
        this.item = item;
        this.count = count;
        this.damage = damage;
        this.tag = tag;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CREATE_ITEM_STACK; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String itemRes = this.item.run(context).str();
        if (itemRes == null) {
            ; // TODO log error once;
            return ScriptValue.EMPTY_ITEM_STACK;
        }
        ResourceLocation itemLoc;
        try {
            itemLoc = new ResourceLocation(itemRes);
        } catch (ResourceLocationException e) {
            ; // TODO log error once
            return ScriptValue.EMPTY_ITEM_STACK;
        }
        ItemStack stack = new ItemStack(OgdenRegistryUtils.getItemFromId(itemLoc));
        if (this.count != null) {
            Number countRes = this.count.run(context).num();
            if (countRes == null) {
                ; // TODO log error once
            } else {
                int countVal = Mth.clamp(countRes.intValue(), 0, stack.getMaxStackSize());
                if (countVal == 0)
                    return ScriptValue.EMPTY_ITEM_STACK;
                stack.setCount(countVal);
            }
        }
        if (this.tag != null) {
            ScriptValue tagRes = this.tag.run(context);
            if (tagRes.map() == null) {
                ; // TODO log error once
            } else if (!(tagRes.toNbt() instanceof CompoundTag ctag)) {
                ; // TODO log error once
            } else {
                stack.setTag(ctag);
            }
        }
        if (stack.isDamageableItem() && this.damage != null) {
            Number damageRes = this.damage.run(context).num();
            if (damageRes == null) {
                ; // TODO log error once
            } else {
                stack.setDamageValue(Mth.clamp(damageRes.intValue(), 0, stack.getMaxDamage() + 1));
            }
        }
        return ScriptValue.itemStack(stack);
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("item"))
                throw new JsonParseException("Create item stack instruction missing parameter 'item'");
            ScriptValueSupplier item = ScriptValueSupplier.fromJson(obj.get("item"));
            ScriptValueSupplier count = obj.has("count") ? ScriptValueSupplier.fromJson(obj.get("count")) : null;
            ScriptValueSupplier damage = obj.has("damage") ? ScriptValueSupplier.fromJson(obj.get("damage")) : null;
            ScriptValueSupplier tag = obj.has("tag") ? ScriptValueSupplier.fromJson(obj.get("tag")) : null;
            return new CreateItemStackInstruction(item, count, damage, tag);
        }
    }

}
