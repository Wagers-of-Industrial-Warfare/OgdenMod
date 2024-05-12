package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class AddIndexToPathInstruction extends TwoObjectOperatorInstruction {

    public AddIndexToPathInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.ADD_INDEX_TO_PATH; }

    @Override
    protected ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        String path = left.str();
        if (path == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number index = right.num();
        if (index == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        return ScriptValue.string(path + "[" + index.intValue() + "]");
    }

    public static class Serializer extends TwoObjectOperatorInstruction.Serializer {
        public Serializer() { super(AddIndexToPathInstruction::new); }
        @Override protected String getLeftKey() { return "path"; }
        @Override protected String getRightKey() { return "index"; }
    }

}
