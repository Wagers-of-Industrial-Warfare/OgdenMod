package rbasamoyai.ogden.firearms.scripting.instructions.logical;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class NotInstruction implements ScriptInstruction {

    private final ScriptValueSupplier operand;

    public NotInstruction(ScriptValueSupplier operand) {
        this.operand = operand;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.NOT; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        Boolean opRes = this.operand.run(context).bool();
        if (opRes == null) {
            ; // TODO: log error once
            return ScriptValue.FALSE;
        }
        return ScriptValue.bool(!opRes);
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("value"))
                throw new JsonSyntaxException("Logical instruction missing parameter 'value'");
            ScriptValueSupplier operand = ScriptValueSupplier.fromJson(obj.get("value"));
            return new NotInstruction(operand);
        }
    }

}
