package rbasamoyai.ogden.firearms.scripting.instructions.string;

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

public class SubstringInstruction implements ScriptInstruction {

    private final ScriptValueSupplier str;
    private final ScriptValueSupplier start;
    private final ScriptValueSupplier end;

    public SubstringInstruction(ScriptValueSupplier str, ScriptValueSupplier start, ScriptValueSupplier end) {
        this.str = str;
        this.start = start;
        this.end = end;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SUBSTRING; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String strRes = this.str.run(context).str();
        if (strRes == null) {
            ; // TODO log error once
            return ScriptValue.EMPTY_STRING;
        }
        Number startRes = this.start.run(context).num();
        if (startRes == null) {
            ; // TODO log error once
            return ScriptValue.string(strRes);
        }
        int si = startRes.intValue();
        si = si < 0 ? strRes.length() : Math.min(si, strRes.length());
        Number endRes = this.end.run(context).num();
        if (endRes == null) {
            ; // TODO log error once
            return ScriptValue.string(strRes);
        }
        int ei = endRes.intValue();
        ei = ei < 0 ? strRes.length() : Math.min(ei, strRes.length());
        if (si > ei) {
            ; // TODO log error once
            return ScriptValue.string(strRes);
        }
        return ScriptValue.string(strRes.substring(si, ei));
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("string"))
                throw new JsonParseException("Substring instruction missing parameter 'string'");
            if (!obj.has("start"))
                throw new JsonParseException("Substring instruction missing parameter 'start'");
            ScriptValueSupplier str = ScriptValueSupplier.fromJson(obj.get("string"));
            ScriptValueSupplier start = ScriptValueSupplier.fromJson(obj.get("start"));
            ScriptValueSupplier end = obj.has("end") ? ScriptValueSupplier.fromJson(obj.get("end")) : ScriptValue.ofInt(-1);
            return new SubstringInstruction(str, start, end);
        }
    }

}
