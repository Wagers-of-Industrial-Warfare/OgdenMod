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

public class RemoveListElementsInstruction implements ScriptInstruction {

    private final ScriptValueSupplier list;
    private final ScriptValueSupplier start;
    private final ScriptValueSupplier end;

    public RemoveListElementsInstruction(ScriptValueSupplier list, ScriptValueSupplier start, ScriptValueSupplier end) {
        this.list = list;
        this.start = start;
        this.end = end;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.REMOVE_LIST_ELEMENTS; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ScriptValue listRes = this.list.run(context);
        List<ScriptValue> listVal = listRes.list();
        if (listVal == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }

        Number startRes = this.start.run(context).num();
        if (startRes == null) {
            ; // TODO log error once
            return listRes;
        }
        int si = startRes.intValue();
        si = si < 0 ? listVal.size() : Math.min(si, listVal.size());

        Number endRes = this.end.run(context).num();
        int ei = endRes == null ? si + 1 : endRes.intValue();
        ei = ei < 0 ? listVal.size() : Math.min(ei, listVal.size());
        if (si > ei) {
            ; // TODO log error once
            return listRes;
        }
        listVal.subList(si, ei).clear();
        return listRes;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("list"))
                throw new JsonParseException("Remove list elements instruction missing parameter 'list'");
            if (!obj.has("start"))
                throw new JsonParseException("Remove list elements instruction missing parameter 'start'");
            ScriptValueSupplier list = ScriptValueSupplier.fromJson(obj.get("list"));
            ScriptValueSupplier start = ScriptValueSupplier.fromJson(obj.get("start"));
            ScriptValueSupplier end = obj.has("end") ? ScriptValueSupplier.fromJson(obj.get("end")) : ScriptValue.VOID;
            return new RemoveListElementsInstruction(list, start, end);
        }
    }

}
