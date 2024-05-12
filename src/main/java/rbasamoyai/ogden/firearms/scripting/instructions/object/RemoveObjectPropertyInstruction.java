package rbasamoyai.ogden.firearms.scripting.instructions.object;

import java.util.Map;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RemoveObjectPropertyInstruction extends KeyOnlyObjectPropertyInstruction {

    public RemoveObjectPropertyInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.REMOVE_OBJECT_PROPERTY; }

    @Override
    protected ScriptValue operateOnMap(Map<String, ScriptValue> map, String key) {
        map.remove(key);
        return ScriptValue.VOID;
    }

    public static class Serializer extends TwoObjectOperatorInstruction.Serializer {
        public Serializer() { super(RemoveObjectPropertyInstruction::new); }
    }

}
