package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SubtractInstruction extends TwoNumberOperatorInstruction {

    public SubtractInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        return ScriptValue.ofDouble(left.doubleValue() - right.doubleValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SUBTRACT; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(SubtractInstruction::new); }
    }

}
