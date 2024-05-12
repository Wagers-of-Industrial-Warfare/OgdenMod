package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class ItemStackPropertyInstruction implements ScriptInstruction {

    private final ScriptValueSupplier stack;

    protected ItemStackPropertyInstruction(ScriptValueSupplier stack) {
        this.stack = stack;
    }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack stackRes = this.stack.run(context).stack();
        if (stackRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        return this.operate(stackRes);
    }

    protected abstract ScriptValue operate(ItemStack itemStack);

    public static abstract class Serializer implements ScriptInstructionSerializer {
        private final Function<ScriptValueSupplier, ScriptInstruction> constructor;

        protected Serializer(Function<ScriptValueSupplier, ScriptInstruction> constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("stack"))
                throw new JsonParseException("Item stack property instruction missing parameter 'stack'");
            ScriptValueSupplier stack = ScriptValueSupplier.fromJson(obj.get("stack"));
            return this.constructor.apply(stack);
        }
    }

}
