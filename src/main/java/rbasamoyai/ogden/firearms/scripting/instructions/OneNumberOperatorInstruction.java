package rbasamoyai.ogden.firearms.scripting.instructions;

import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class OneNumberOperatorInstruction implements ScriptInstruction {

    protected final ScriptValueSupplier operand;

    protected OneNumberOperatorInstruction(ScriptValueSupplier operand) {
        this.operand = operand;
    }

    @Nonnull
    @Override
    public final ScriptValue run(ScriptContext context) {
        Number opRes = this.operand.run(context).num();
        if (opRes == null) {
            ; // TODO: log error once
            return ScriptValue.ZERO;
        }
        return this.operate(opRes);
    }

    protected abstract ScriptValue operate(Number operator);

    public static abstract class Serializer implements ScriptInstructionSerializer {
        private final Function<ScriptValueSupplier, ScriptInstruction> constructor;

        protected Serializer(Function<ScriptValueSupplier, ScriptInstruction> constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("value"))
                throw new JsonSyntaxException("Numerical instruction missing parameter 'value'");
            ScriptValueSupplier operand = ScriptValueSupplier.fromJson(obj.get("value"));
            return this.constructor.apply(operand);
        }
    }

}
