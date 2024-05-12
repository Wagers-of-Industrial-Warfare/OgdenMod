package rbasamoyai.ogden.firearms.scripting.instructions.list;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.Mth;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SublistInstruction implements ScriptInstruction {

    private final ScriptValueSupplier list;
    private final ScriptValueSupplier start;
    private final ScriptValueSupplier end;

    public SublistInstruction(ScriptValueSupplier list, ScriptValueSupplier start, ScriptValueSupplier end) {
        this.list = list;
        this.start = start;
        this.end = end;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SUBLIST; }

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
        int si = Mth.clamp(startRes.intValue(), 0, listVal.size());

        Number endRes = this.end.run(context).num();
        if (endRes == null) {
            ; // TODO log error once
            return listRes;
        }
        int ei = Mth.clamp(endRes.intValue(), 0, listVal.size());

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
                throw new JsonParseException("Sublist instruction missing parameter 'list'");
            if (!obj.has("start"))
                throw new JsonParseException("Sublist instruction missing parameter 'start'");
            if (!obj.has("end"))
                throw new JsonParseException("Sublist instruction missing parameter 'end'");
            ScriptValueSupplier list = ScriptValueSupplier.fromJson(obj.get("list"));
            ScriptValueSupplier start = ScriptValueSupplier.fromJson(obj.get("start"));
            ScriptValueSupplier end = ScriptValueSupplier.fromJson(obj.get("end"));
            return new SublistInstruction(list, start, end);
        }
    }

}
