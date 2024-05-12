package rbasamoyai.ogden.firearms.scripting.instructions.list;

import java.util.List;

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

public class AddListElementInstruction implements ScriptInstruction {

    private final ScriptValueSupplier target;
    private final ScriptValueSupplier source;
    private final ScriptValueSupplier index;

    public AddListElementInstruction(ScriptValueSupplier target, ScriptValueSupplier source, ScriptValueSupplier index) {
        this.target = target;
        this.source = source;
        this.index = index;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.ADD_LIST_ELEMENT; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ScriptValue listRes = this.target.run(context);
        List<ScriptValue> listVal = listRes.list();
        if (listVal == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number indexRes = this.index.run(context).num();
        int indexVal = indexRes == null ? -1 : indexRes.intValue();
        indexVal = indexVal < 0 ? listVal.size() : Math.min(indexVal, listVal.size());
        listVal.add(indexVal, this.source.run(context));
        return listRes;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("target"))
                throw new JsonParseException("Add list element instruction missing parameter 'target'");
            if (!obj.has("element"))
                throw new JsonParseException("Add list element instruction missing parameter 'element'");
            ScriptValueSupplier target = ScriptValueSupplier.fromJson(obj.get("target"));
            ScriptValueSupplier element = ScriptValueSupplier.fromJson(obj.get("element"));
            ScriptValueSupplier index = obj.has("index") ? ScriptValueSupplier.fromJson(obj.get("index")) : ScriptValue.ofInt(-1);
            return new AddListElementInstruction(target, element, index);
        }
    }

}
