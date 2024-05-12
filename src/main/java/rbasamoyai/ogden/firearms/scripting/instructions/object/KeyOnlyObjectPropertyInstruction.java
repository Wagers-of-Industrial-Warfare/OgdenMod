package rbasamoyai.ogden.firearms.scripting.instructions.object;

import java.util.Map;
import java.util.function.BiFunction;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;

public abstract class KeyOnlyObjectPropertyInstruction extends TwoObjectOperatorInstruction {

    protected KeyOnlyObjectPropertyInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        super(left, right);
    }

    @Override
    protected final ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        Map<String, ScriptValue> map = left.map();
        if (map == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        String key = right.str();
        if (key == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        return this.operateOnMap(map, key);
    }

    protected abstract ScriptValue operateOnMap(Map<String, ScriptValue> map, String key);

    public static abstract class Serializer extends TwoObjectOperatorInstruction.Serializer {
        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            super(constructor);
        }

        @Override protected String getLeftKey() { return "object"; }
        @Override protected String getRightKey() { return "value"; }
    }

}
