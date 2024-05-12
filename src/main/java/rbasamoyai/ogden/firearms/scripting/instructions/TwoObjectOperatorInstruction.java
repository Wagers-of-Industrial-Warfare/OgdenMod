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

public abstract class TwoObjectOperatorInstruction implements ScriptInstruction {

    protected final ScriptValueSupplier left;
    protected final ScriptValueSupplier right;

    protected TwoObjectOperatorInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        this.left = left;
        this.right = right;
    }

    @Nonnull
    @Override
    public final ScriptValue run(ScriptContext context) {
        ScriptValue leftRes = this.left.run(context);
        ScriptValue rightRes = this.right.run(context);
        return this.operate(leftRes, rightRes, context);
    }

    protected abstract ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context);

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
                throw new JsonSyntaxException("Instruction missing parameter '" + leftArg + "'");
            if (!obj.has(rightArg))
                throw new JsonSyntaxException("Instruction missing parameter '" + rightArg + "'");
            ScriptValueSupplier left = ScriptValueSupplier.fromJson(obj.get(leftArg));
            ScriptValueSupplier right = ScriptValueSupplier.fromJson(obj.get(rightArg));
            return this.constructor.apply(left, right);
        }

        protected String getLeftKey() { return "left"; }
        protected String getRightKey() { return "right"; }
    }

}
