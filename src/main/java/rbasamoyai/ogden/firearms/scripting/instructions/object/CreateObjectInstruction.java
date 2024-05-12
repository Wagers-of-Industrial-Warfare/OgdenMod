package rbasamoyai.ogden.firearms.scripting.instructions.object;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

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

public class CreateObjectInstruction implements ScriptInstruction {

    private final Map<String, ScriptValueSupplier> objectSup;

    public CreateObjectInstruction(Map<String, ScriptValueSupplier> objectSup) {
        this.objectSup = objectSup;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CREATE_OBJECT; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        Map<String, ScriptValue> res = new HashMap<>();
        for (Map.Entry<String, ScriptValueSupplier> entry : this.objectSup.entrySet())
            res.put(entry.getKey(), entry.getValue().run(context));
        return new ScriptValue(res);
    }

    public static class Serializer implements ScriptInstructionSerializer {

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            JsonObject mapObj = GsonHelper.getAsJsonObject(obj, "data");
            Map<String, ScriptValueSupplier> objSup = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : mapObj.entrySet())
                objSup.put(entry.getKey(), ScriptValueSupplier.fromJson(entry.getValue()));
            return new CreateObjectInstruction(objSup);
        }
    }

}
