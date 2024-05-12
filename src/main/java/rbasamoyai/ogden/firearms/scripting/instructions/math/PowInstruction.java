package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class PowInstruction extends TwoNumberOperatorInstruction {

    public PowInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        double value = Math.pow(left.doubleValue(), right.doubleValue());
        return ScriptValue.ofDouble(Double.isFinite(value) ? value : left.doubleValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.POW; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(PowInstruction::new); }

        @Override protected String getLeftKey() { return "base"; }
        @Override protected String getRightKey() { return "power"; }
    }

}
