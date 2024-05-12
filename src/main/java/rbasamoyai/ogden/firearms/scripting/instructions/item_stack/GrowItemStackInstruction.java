package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class GrowItemStackInstruction extends ItemStackCountInstruction {

    public GrowItemStackInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.GROW_ITEM_STACK; }

    @Override
    protected ItemStack operate(ItemStack stack, int count) {
        stack.grow(count);
        stack.setCount(Mth.clamp(stack.getCount(), 0, stack.getMaxStackSize()));
        return stack;
    }

    public static class Serializer extends ItemStackCountInstruction.Serializer {
        public Serializer() { super(GrowItemStackInstruction::new); }
    }

}
