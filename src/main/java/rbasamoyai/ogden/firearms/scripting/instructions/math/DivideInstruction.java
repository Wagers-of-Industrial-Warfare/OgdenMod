package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class DivideInstruction extends TwoNumberOperatorInstruction {

    public DivideInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        return ScriptValue.ofDouble(Math.abs(right.doubleValue()) < 1e-6d ? 0d : left.doubleValue() / right.doubleValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.DIVIDE; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(DivideInstruction::new); }
    }

}
