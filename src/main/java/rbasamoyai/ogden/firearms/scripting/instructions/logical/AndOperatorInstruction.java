package rbasamoyai.ogden.firearms.scripting.instructions.logical;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class AndOperatorInstruction extends TwoBooleanOperatorInstruction {

    public AndOperatorInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.AND; }

    @Override protected boolean operate(boolean left, boolean right) { return left && right; }

    public static class Serializer extends TwoBooleanOperatorInstruction.Serializer {
        public Serializer() { super(AndOperatorInstruction::new); }
    }

}
