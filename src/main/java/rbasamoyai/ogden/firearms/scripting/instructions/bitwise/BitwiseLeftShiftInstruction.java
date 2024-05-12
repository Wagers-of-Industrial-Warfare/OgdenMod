package rbasamoyai.ogden.firearms.scripting.instructions.bitwise;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class BitwiseLeftShiftInstruction extends TwoNumberOperatorInstruction {

    public BitwiseLeftShiftInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        return ScriptValue.ofLong(left.longValue() << right.longValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.BITWISE_LEFT_SHIFT; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(BitwiseLeftShiftInstruction::new); }
    }

}
