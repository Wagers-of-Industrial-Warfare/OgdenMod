package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.OneNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class AbsInstruction extends OneNumberOperatorInstruction {

    public AbsInstruction(ScriptValueSupplier operand) { super(operand); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.ABS; }

    @Override
    protected ScriptValue operate(Number operator) {
        return ScriptValue.ofDouble(Math.abs(operator.doubleValue()));
    }

    public static class Serializer extends OneNumberOperatorInstruction.Serializer {
        public Serializer() { super(AbsInstruction::new); }
    }

}
