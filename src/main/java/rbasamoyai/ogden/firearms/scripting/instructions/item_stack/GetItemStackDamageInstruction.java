package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class GetItemStackDamageInstruction extends ItemStackPropertyInstruction {

    public GetItemStackDamageInstruction(ScriptValueSupplier stack) { super(stack); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.GET_ITEM_STACK_DAMAGE; }

    @Override
    protected ScriptValue operate(ItemStack itemStack) {
        return ScriptValue.ofInt(itemStack.getDamageValue());
    }

    public static class Serializer extends ItemStackPropertyInstruction.Serializer {
        public Serializer() { super(GetItemStackDamageInstruction::new); }
    }

}
