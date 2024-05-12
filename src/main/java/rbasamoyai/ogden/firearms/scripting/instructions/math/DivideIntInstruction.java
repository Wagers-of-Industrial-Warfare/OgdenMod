package rbasamoyai.ogden.firearms.scripting.instructions.math;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class DivideIntInstruction extends TwoNumberOperatorInstruction {

    public DivideIntInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        return ScriptValue.ofLong(right.longValue() == 0 ? 0 : left.longValue() / right.longValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.DIVIDE_INT; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(DivideIntInstruction::new); }
    }

}
