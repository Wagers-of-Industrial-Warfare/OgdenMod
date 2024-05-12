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

public class ChangeStateInstruction implements ScriptInstruction {

    private final ScriptValueSupplier state;

    public ChangeStateInstruction(ScriptValueSupplier state) {
        this.state = state;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.CHANGE_STATE; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String stateRes = this.state.run(context).str();
        if (stateRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        CompoundTag tag = context.stack().getOrCreateTag();
        tag.putString("CurrentTime", stateRes);
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("state"))
                throw new JsonParseException("Change state instruction missing parameter 'state'");
            ScriptValueSupplier state = ScriptValueSupplier.fromJson(obj.get("state"));
            return new ChangeStateInstruction(state);
        }
    }

}
