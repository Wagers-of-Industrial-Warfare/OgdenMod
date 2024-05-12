package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class IsSameItemInstruction extends TwoObjectOperatorInstruction {

    public IsSameItemInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        super(left, right);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.IS_SAME_ITEM; }

    @Override
    protected ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        ItemStack leftRes = left.run(context).stack();
        if (leftRes == null) {
            ; // TODO log error once
            return ScriptValue.FALSE;
        }
        ItemStack rightRes = right.run(context).stack();
        if (rightRes == null) {
            ; // TODO log error once
            return ScriptValue.FALSE;
        }
        Item leftItem = leftRes.isEmpty() ? Items.AIR : leftRes.getItem();
        Item rightItem = rightRes.isEmpty() ? Items.AIR : rightRes.getItem();
        return ScriptValue.bool(leftItem == rightItem);
    }

    public static class Serializer extends TwoObjectOperatorInstruction.Serializer {
        public Serializer() { super(IsSameItemInstruction::new); }
    }

}
