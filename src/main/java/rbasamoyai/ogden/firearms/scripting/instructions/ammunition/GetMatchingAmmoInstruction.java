package rbasamoyai.ogden.firearms.scripting.instructions.ammunition;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class GetMatchingAmmoInstruction implements ScriptInstruction {

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.GET_MATCHING_AMMO; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        return ScriptValue.itemStack(context.entity().getProjectile(context.stack()));
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            return new GetMatchingAmmoInstruction();
        }
    }

}
