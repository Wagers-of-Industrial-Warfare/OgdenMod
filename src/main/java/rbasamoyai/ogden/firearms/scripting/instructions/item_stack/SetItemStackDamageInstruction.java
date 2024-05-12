package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SetItemStackDamageInstruction extends ItemStackDamageInstruction {

    public SetItemStackDamageInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SET_ITEM_STACK_DAMAGE; }

    @Override
    protected ItemStack operate(ItemStack stack, int damage) {
        stack.setDamageValue(Mth.clamp(damage, 0, stack.getMaxDamage() + 1));
        return stack;
    }

    public static class Serializer extends ItemStackDamageInstruction.Serializer {
        public Serializer() { super(SetItemStackDamageInstruction::new); }
    }

}
