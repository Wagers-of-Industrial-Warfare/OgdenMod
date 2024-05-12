package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SplitItemStackInstruction extends ItemStackCountInstruction {

    public SplitItemStackInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SPLIT_ITEM_STACK; }

    @Override
    protected ItemStack operate(ItemStack stack, int count) {
        ItemStack newStack = stack.split(count);
        stack.setCount(Mth.clamp(stack.getCount(), 0, stack.getMaxStackSize()));
        newStack.setCount(Mth.clamp(newStack.getCount(), 0, newStack.getMaxStackSize()));
        return newStack;
    }

    public static class Serializer extends ItemStackCountInstruction.Serializer {
        public Serializer() { super(SplitItemStackInstruction::new); }
    }

}
