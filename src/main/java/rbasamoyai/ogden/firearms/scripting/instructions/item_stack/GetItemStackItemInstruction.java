package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class GetItemStackItemInstruction extends ItemStackPropertyInstruction {

    public GetItemStackItemInstruction(ScriptValueSupplier stack) { super(stack); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.GET_ITEM_STACK_ITEM; }

    @Override
    protected ScriptValue operate(ItemStack itemStack) {
        return ScriptValue.string(OgdenRegistryUtils.getItemId(itemStack.isEmpty() ? Items.AIR : itemStack.getItem()).toString());
    }

    public static class Serializer extends ItemStackPropertyInstruction.Serializer {
        public Serializer() { super(GetItemStackItemInstruction::new); }
    }

}
