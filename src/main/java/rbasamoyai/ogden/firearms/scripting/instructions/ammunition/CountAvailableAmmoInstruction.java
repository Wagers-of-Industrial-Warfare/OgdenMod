package rbasamoyai.ogden.firearms.scripting.instructions.ammunition;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.ammunition.AmmoUtils;
import rbasamoyai.ogden.firearms.config.FirearmAcceptedAmmunitionHandler;
import rbasamoyai.ogden.firearms.config.FirearmAmmoPredicate;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CountAvailableAmmoInstruction implements ScriptInstruction {

    private final ScriptValueSupplier countClips;

    public CountAvailableAmmoInstruction(ScriptValueSupplier countClips) {
        this.countClips = countClips;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.COUNT_AVAILABLE_AMMO; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack stack = context.stack();
        Boolean countClipsRes = this.countClips.run(context).bool();
        boolean countClipsFlag;
        if (countClipsRes == null) {
            ; // TODO log error once
            countClipsFlag = false;
        } else {
            countClipsFlag = countClipsRes;
        }
        FirearmAmmoPredicate predicate = FirearmAcceptedAmmunitionHandler.getAmmoPredicate(stack);
        Container container = AmmoUtils.getEntityInventory(context.entity());
        return ScriptValue.ofInt(countClipsFlag ? AmmoUtils.countAvailableClips(container, predicate) : AmmoUtils.countAvailableAmmo(container, predicate));
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            ScriptValueSupplier countClips = obj.has("count_clips") ? ScriptValueSupplier.fromJson(obj.get("count_clips")) : ScriptValue.FALSE;
            return new CountAvailableAmmoInstruction(countClips);
        }
    }

}
