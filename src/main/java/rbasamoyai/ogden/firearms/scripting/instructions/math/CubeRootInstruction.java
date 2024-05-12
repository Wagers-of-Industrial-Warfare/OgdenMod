package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.OneNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CubeRootInstruction extends OneNumberOperatorInstruction {

    public CubeRootInstruction(ScriptValueSupplier operand) { super(operand); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CUBE_ROOT; }

    @Override protected ScriptValue operate(Number operator) { return ScriptValue.ofDouble(Math.pow(operator.doubleValue(), 1 / 3d)); }

    public static class Serializer extends OneNumberOperatorInstruction.Serializer {
        public Serializer() { super(CubeRootInstruction::new); }
    }

}
