package rbasamoyai.ogden.firearms.scripting.instructions;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class TwoNumberOperatorInstruction implements ScriptInstruction {

    protected final ScriptValueSupplier left;
    protected final ScriptValueSupplier right;

    protected TwoNumberOperatorInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        this.left = left;
        this.right = right;
    }

    @Nonnull
    @Override
    public final ScriptValue run(ScriptContext context) {
        Number leftRes = this.left.run(context).num();
        Number rightRes = this.right.run(context).num();
        if (leftRes == null || rightRes == null) {
            ; // TODO: log error once
            return ScriptValue.ZERO;
        }
        return this.operate(leftRes, rightRes);
    }

    protected abstract ScriptValue operate(Number left, Number right);

    public static abstract class Serializer implements ScriptInstructionSerializer {
        private final BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor;

        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            String leftArg = this.getLeftKey();
            String rightArg = this.getRightKey();
            if (!obj.has(leftArg))
                throw new JsonSyntaxException("Numerical instruction missing parameter '" + leftArg + "'");
            if (!obj.has(rightArg))
                throw new JsonSyntaxException("Numerical instruction missing parameter '" + rightArg + "'");
            ScriptValueSupplier left = ScriptValueSupplier.fromJson(obj.get(leftArg));
            ScriptValueSupplier right = ScriptValueSupplier.fromJson(obj.get(rightArg));
            return this.constructor.apply(left, right);
        }

        protected String getLeftKey() { return "left"; }
        protected String getRightKey() { return "right"; }
    }

}
