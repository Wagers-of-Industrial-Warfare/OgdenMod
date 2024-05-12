package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.OneNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class LnInstruction extends OneNumberOperatorInstruction {

    public LnInstruction(ScriptValueSupplier operand) { super(operand); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.LN; }

    @Override
    protected ScriptValue operate(Number operator) {
        return ScriptValue.ofDouble(Math.log(operator.doubleValue()));
    }

    public static class Serializer extends OneNumberOperatorInstruction.Serializer {
        public Serializer() { super(LnInstruction::new); }
    }

}
