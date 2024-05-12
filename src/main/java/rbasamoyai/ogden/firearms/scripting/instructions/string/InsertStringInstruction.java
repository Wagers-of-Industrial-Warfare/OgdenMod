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

public class InsertStringInstruction implements ScriptInstruction {

    private final ScriptValueSupplier target;
    private final ScriptValueSupplier source;
    private final ScriptValueSupplier index;

    public InsertStringInstruction(ScriptValueSupplier target, ScriptValueSupplier source, ScriptValueSupplier index) {
        this.target = target;
        this.source = source;
        this.index = index;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.INSERT_STRING; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String targetRes = this.target.run(context).str();
        if (targetRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }

        String sourceRes = this.source.run(context).str();
        if (sourceRes == null) {
            ; // TODO log error once
            return ScriptValue.string(targetRes);
        }
        Number indexRes = this.index.run(context).num();
        if (indexRes == null) {
            ; // TODO log error once
            return ScriptValue.string(targetRes);
        }
        int i = indexRes.intValue();
        i = i < 0 ? targetRes.length() : Math.min(i, targetRes.length());
        return ScriptValue.string(new StringBuilder(targetRes).insert(i, sourceRes).toString());
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("target"))
                throw new JsonParseException("Insert string instruction missing parameter 'target'");
            if (!obj.has("source"))
                throw new JsonParseException("Insert string instruction missing parameter 'source'");
            if (!obj.has("index"))
                throw new JsonParseException("Insert string instruction missing parameter 'index'");
            ScriptValueSupplier target = ScriptValueSupplier.fromJson(obj.get("target"));
            ScriptValueSupplier source = ScriptValueSupplier.fromJson(obj.get("source"));
            ScriptValueSupplier index = ScriptValueSupplier.fromJson(obj.get("index"));
            return new InsertStringInstruction(target, source, index);
        }
    }

}
