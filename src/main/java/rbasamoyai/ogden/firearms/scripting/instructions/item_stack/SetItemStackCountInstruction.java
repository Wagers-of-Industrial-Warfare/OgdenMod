package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SetItemStackCountInstruction extends ItemStackCountInstruction {

    public SetItemStackCountInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SET_ITEM_STACK_COUNT; }

    @Override
    protected ItemStack operate(ItemStack stack, int count) {
        stack.setCount(Mth.clamp(count, 0, stack.getMaxStackSize()));
        return stack;
    }

    public static class Serializer extends ItemStackCountInstruction.Serializer {
        public Serializer() { super(SetItemStackCountInstruction::new); }
    }

}
