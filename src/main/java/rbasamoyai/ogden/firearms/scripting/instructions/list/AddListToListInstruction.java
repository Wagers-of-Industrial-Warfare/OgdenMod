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

public class AddListToListInstruction implements ScriptInstruction {

    private final ScriptValueSupplier target;
    private final ScriptValueSupplier source;
    private final ScriptValueSupplier index;

    public AddListToListInstruction(ScriptValueSupplier target, ScriptValueSupplier source, ScriptValueSupplier index) {
        this.target = target;
        this.source = source;
        this.index = index;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.ADD_LIST_TO_LIST; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ScriptValue targetRes = this.target.run(context);
        List<ScriptValue> targetVal = targetRes.list();
        if (targetVal == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        List<ScriptValue> sourceRes = this.source.run(context).list();
        if (sourceRes == null) {
            ; // TODO log error once
            return targetRes;
        }
        Number indexRes = this.index.run(context).num();
        int indexVal = indexRes == null ? -1 : indexRes.intValue();
        indexVal = indexVal < 0 ? targetVal.size() : Math.min(indexVal, targetVal.size());
        targetVal.addAll(indexVal, sourceRes);
        return targetRes;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("target"))
                throw new JsonParseException("Add list to list instruction missing parameter 'target'");
            if (!obj.has("source"))
                throw new JsonParseException("Add list to list instruction missing parameter 'source'");
            ScriptValueSupplier target = ScriptValueSupplier.fromJson(obj.get("target"));
            ScriptValueSupplier source = ScriptValueSupplier.fromJson(obj.get("source"));
            ScriptValueSupplier index = obj.has("index") ? ScriptValueSupplier.fromJson(obj.get("index")) : ScriptValue.ofInt(-1);
            return new AddListToListInstruction(target, source, index);
        }
    }

}
