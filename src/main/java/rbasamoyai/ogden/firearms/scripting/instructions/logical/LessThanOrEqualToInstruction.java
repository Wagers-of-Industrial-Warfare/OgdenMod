package rbasamoyai.ogden.firearms.scripting.instructions.logical;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoNumberOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class LessThanOrEqualToInstruction extends TwoNumberOperatorInstruction {

    public LessThanOrEqualToInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override
    protected ScriptValue operate(Number left, Number right) {
        return ScriptValue.bool(left.doubleValue() <= right.doubleValue());
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.LESS_THAN_OR_EQUAL_TO; }

    public static class Serializer extends TwoNumberOperatorInstruction.Serializer {
        public Serializer() { super(LessThanOrEqualToInstruction::new); }
    }

}
