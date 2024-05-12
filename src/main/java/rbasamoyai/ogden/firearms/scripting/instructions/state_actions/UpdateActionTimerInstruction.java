package rbasamoyai.ogden.firearms.scripting.instructions.state_actions;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.OgdenFirearmItem;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class UpdateActionTimerInstruction implements ScriptInstruction {

    public UpdateActionTimerInstruction() {
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.UPDATE_ACTION_TIMER; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack stack = context.stack();
        if (!(stack.getItem() instanceof OgdenFirearmItem firearm)) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("NextTime", Tag.TAG_FLOAT))
            tag.putFloat("NextTime", 0f);
        float time = tag.getFloat("NextTime");
        tag.putFloat("CurrentTime", time);
        time += firearm.getPassedActionTime(context);
        tag.putFloat("NextTime", time);
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            return new UpdateActionTimerInstruction();
        }
    }

}
