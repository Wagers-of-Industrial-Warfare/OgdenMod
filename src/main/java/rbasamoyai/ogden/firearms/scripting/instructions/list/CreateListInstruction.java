package rbasamoyai.ogden.firearms.scripting.instructions.list;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.GsonHelper;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CreateListInstruction implements ScriptInstruction {

    private final List<ScriptValueSupplier> values;

    public CreateListInstruction(List<ScriptValueSupplier> values) {
        this.values = values;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CREATE_LIST; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        List<ScriptValue> list = new ArrayList<>();
        for (ScriptValueSupplier value : this.values)
            list.add(value.run(context));
        return new ScriptValue(list);
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            JsonArray arr = GsonHelper.getAsJsonArray(obj, "elements", new JsonArray());
            List<ScriptValueSupplier> values = new ArrayList<>();
            for (JsonElement el : arr)
                values.add(ScriptValueSupplier.fromJson(el));
            return new CreateListInstruction(values);
        }
    }

}
