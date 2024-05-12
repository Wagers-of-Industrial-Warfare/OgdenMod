package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.OneNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CubeInstruction extends OneNumberOperatorInstruction {

    public CubeInstruction(ScriptValueSupplier operand) { super(operand); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CUBE; }

    @Override
    protected ScriptValue operate(Number operator) {
        double value = operator.doubleValue();
        return ScriptValue.ofDouble(value * value * value);
    }

    public static class Serializer extends OneNumberOperatorInstruction.Serializer {
        public Serializer() { super(CubeInstruction::new); }
    }

}
