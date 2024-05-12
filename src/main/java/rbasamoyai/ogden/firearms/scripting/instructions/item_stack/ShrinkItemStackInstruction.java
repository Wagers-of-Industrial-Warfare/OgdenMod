package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class ShrinkItemStackInstruction extends GrowItemStackInstruction {

    public ShrinkItemStackInstruction(ScriptValueSupplier left, ScriptValueSupplier right) { super(left, right); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SHRINK_ITEM_STACK; }

    @Override
    protected ItemStack operate(ItemStack stack, int count) {
        return super.operate(stack, -count);
    }

    public static class Serializer extends ItemStackCountInstruction.Serializer {
        public Serializer() { super(ShrinkItemStackInstruction::new); }
    }

}
