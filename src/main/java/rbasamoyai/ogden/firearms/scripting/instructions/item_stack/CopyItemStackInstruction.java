package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CopyItemStackInstruction implements ScriptInstruction {

    private final ScriptValueSupplier item;

    public CopyItemStackInstruction(ScriptValueSupplier item) {
        this.item = item;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.COPY_ITEM_STACK; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack item = this.item.run(context).stack();
        if (item == null) {
            ; // TODO log error once
            return ScriptValue.EMPTY_ITEM_STACK;
        }
        return ScriptValue.itemStack(item.copy());
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("item_stack"))
                throw new JsonSyntaxException("Copy item stack instruction missing parameter 'item_stack'");
            ScriptValueSupplier item = ScriptValueSupplier.fromJson(obj.get("item_stack"));
            return new CopyItemStackInstruction(item);
        }
    }

}
