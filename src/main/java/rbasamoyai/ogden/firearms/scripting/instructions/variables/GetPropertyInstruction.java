package rbasamoyai.ogden.firearms.scripting.instructions.variables;

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

public class GetPropertyInstruction implements ScriptInstruction {

    private final ScriptValueSupplier id;

    public GetPropertyInstruction(ScriptValueSupplier id) {
        this.id = id;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.GET_PROPERTY; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String idRes = this.id.run(context).str();
        if (idRes == null) {
            ; // TODO: log error once
            return ScriptValue.VOID;
        }
        // TODO: Properties access
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("id"))
                throw new JsonParseException("Get property instruction missing parameter 'id'");
            ScriptValueSupplier id = ScriptValueSupplier.fromJson(obj.get("id"));
            return new GetPropertyInstruction(id);
        }
    }

}
