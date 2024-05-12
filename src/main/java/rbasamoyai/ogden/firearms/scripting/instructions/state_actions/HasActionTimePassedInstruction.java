package rbasamoyai.ogden.firearms.scripting.instructions.state_actions;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.nbt.CompoundTag;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class HasActionTimePassedInstruction implements ScriptInstruction {

    private final ScriptValueSupplier time;

    public HasActionTimePassedInstruction(ScriptValueSupplier time) {
        this.time = time;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.HAS_ACTION_TIME_PASSED; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        Number timeRes = this.time.run(context).num();
        if (timeRes == null) {
            ; // TODO log error once
            return ScriptValue.FALSE;
        }
        float t = timeRes.floatValue();
        CompoundTag tag = context.stack().getOrCreateTag();
        float currentTime = tag.getFloat("CurrentTime");
        float nextTime = tag.getFloat("NextTime");
        return ScriptValue.bool(currentTime <= t && t < nextTime);
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("time"))
                throw new JsonParseException("Has action time passed instruction missing parameter 'time'");
            ScriptValueSupplier time = ScriptValueSupplier.fromJson(obj.get("time"));
            return new HasActionTimePassedInstruction(time);
        }
    }

}
