package rbasamoyai.ogden.firearms.scripting.instructions.object;

import java.util.Map;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SetObjectPropertyInstruction implements ScriptInstruction {

    private final ScriptValueSupplier obj;
    private final ScriptValueSupplier key;
    private final ScriptValueSupplier value;

    public SetObjectPropertyInstruction(ScriptValueSupplier obj, ScriptValueSupplier key, ScriptValueSupplier value) {
        this.obj = obj;
        this.key = key;
        this.value = value;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SET_OBJECT_PROPERTY; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        Map<String, ScriptValue> objRes = this.obj.run(context).map();
        if (objRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        String keyRes = this.key.run(context).str();
        if (keyRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        ScriptValue previous = objRes.put(keyRes, this.value.run(context));
        return previous == null ? ScriptValue.VOID : previous;
    }

    public static class Serializer implements ScriptInstructionSerializer {

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("object"))
                throw new JsonParseException("Set object property instruction missing parameter 'object'");
            if (!obj.has("key"))
                throw new JsonParseException("Set object property instruction missing parameter 'key'");
            if (!obj.has("value"))
                throw new JsonParseException("Set object property instruction missing parameter 'value'");
            ScriptValueSupplier objSup = ScriptValueSupplier.fromJson(obj.get("object"));
            ScriptValueSupplier key = ScriptValueSupplier.fromJson(obj.get("key"));
            ScriptValueSupplier value = ScriptValueSupplier.fromJson(obj.get("value"));
            return new SetObjectPropertyInstruction(objSup, key, value);
        }
    }

}
