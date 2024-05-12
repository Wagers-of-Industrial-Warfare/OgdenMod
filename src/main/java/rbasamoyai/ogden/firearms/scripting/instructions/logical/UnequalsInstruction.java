package rbasamoyai.ogden.firearms.scripting.instructions.logical;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class UnequalsInstruction extends TwoObjectOperatorInstruction {

    public UnequalsInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.UNEQUAL; }

    @Override
    protected ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        return ScriptValue.bool(!left.equals(right));
    }

    public static class Serializer extends TwoObjectOperatorInstruction.Serializer {
        public Serializer() { super(UnequalsInstruction::new); }
    }

}
