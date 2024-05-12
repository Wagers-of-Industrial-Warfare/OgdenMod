package rbasamoyai.ogden.firearms.scripting.instructions.logical;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;

public abstract class TwoBooleanOperatorInstruction extends TwoObjectOperatorInstruction {

    protected TwoBooleanOperatorInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        super(left, right);
    }

    @Nonnull
    @Override
    public final ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        Boolean leftRes = this.left.run(context).bool();
        Boolean rightRes = this.right.run(context).bool();
        if (leftRes == null || rightRes == null) {
            ; // TODO: log error once
            return ScriptValue.FALSE;
        }
        return ScriptValue.bool(this.operate(leftRes, rightRes));
    }

    protected abstract boolean operate(boolean left, boolean right);

    public static abstract class Serializer extends TwoObjectOperatorInstruction.Serializer {
        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            super(constructor);
        }
    }

}
