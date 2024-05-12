package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class NthRootInstruction extends TwoNumberOperatorInstruction {

    public NthRootInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        double value = Math.pow(left.doubleValue(), 1 / right.doubleValue());
        return ScriptValue.ofDouble(Double.isFinite(value) ? value : left.doubleValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.NTH_ROOT; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(NthRootInstruction::new); }
    }

}
