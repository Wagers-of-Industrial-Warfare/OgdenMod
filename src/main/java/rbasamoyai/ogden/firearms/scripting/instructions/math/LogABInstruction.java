package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class LogABInstruction extends TwoNumberOperatorInstruction {

    public LogABInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        double value = Math.log(right.doubleValue()) / Math.log(left.doubleValue());
        return ScriptValue.ofDouble(Double.isFinite(value) ? value : 0d);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.LOG_AB; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(LogABInstruction::new); }

        @Override protected String getLeftKey() { return "base"; }
        @Override protected String getRightKey() { return "value"; }
    }

}
