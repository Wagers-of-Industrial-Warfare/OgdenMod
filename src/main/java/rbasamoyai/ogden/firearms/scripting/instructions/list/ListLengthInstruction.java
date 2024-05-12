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

public class ListLengthInstruction implements ScriptInstruction {

    private final ScriptValueSupplier list;

    public ListLengthInstruction(ScriptValueSupplier list) {
        this.list = list;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.LIST_LENGTH; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ScriptValue listRes = this.list.run(context);
        List<ScriptValue> listVal = listRes.list();
        if (listVal == null) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        return ScriptValue.ofInt(listVal.size());
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("list"))
                throw new JsonParseException("Sublist instruction missing parameter 'list'");
            ScriptValueSupplier list = ScriptValueSupplier.fromJson(obj.get("list"));
            return new ListLengthInstruction(list);
        }
    }

}
