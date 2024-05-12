package rbasamoyai.ogden.firearms.scripting.instructions.bitwise;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.OneNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class BitwiseNotInstruction extends OneNumberOperatorInstruction {

    public BitwiseNotInstruction(ScriptValueSupplier operand) { super(operand); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.BITWISE_NOT; }

    @Override
    protected ScriptValue operate(Number operator) {
        return ScriptValue.ofLong(~operator.longValue());
    }

    public static class Serializer extends OneNumberOperatorInstruction.Serializer {
        public Serializer() { super(BitwiseNotInstruction::new); }
    }

}
