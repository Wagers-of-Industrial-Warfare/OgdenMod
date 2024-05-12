package rbasamoyai.ogden.firearms.scripting.instructions.state_actions;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.OgdenFirearmItem;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SyncAnimationInstruction implements ScriptInstruction {

    private final ScriptValueSupplier animationData;

    public SyncAnimationInstruction(ScriptValueSupplier animationData) {
        this.animationData = animationData;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SYNC_ANIMATION; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack stack = context.stack();
        if (!(stack.getItem() instanceof OgdenFirearmItem firearm)) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        ScriptValue dataRes = this.animationData.run(context);
        firearm.syncFirearmAnimation(dataRes, context);
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("animation_data"))
                throw new JsonParseException("Sync animation instruction missing parameter 'animation_data'");
            ScriptValueSupplier animationData = ScriptValueSupplier.fromJson(obj.get("animation_data"));
            return new SyncAnimationInstruction(animationData);
        }
    }

}
