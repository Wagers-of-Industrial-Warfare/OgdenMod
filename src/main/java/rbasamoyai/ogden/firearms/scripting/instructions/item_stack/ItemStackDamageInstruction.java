package rbasamoyai.ogden.firearms.scripting.instructions.item_stack;

import java.util.function.BiFunction;

import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;

public abstract class ItemStackDamageInstruction extends TwoObjectOperatorInstruction {

    protected ItemStackDamageInstruction(ScriptValueSupplier left, ScriptValueSupplier right) {
        super(left, right);
    }

    @Override
    protected final ScriptValue operate(ScriptValue left, ScriptValue right, ScriptContext context) {
        ItemStack stack = left.stack();
        if (stack == null) {
            ; // TODO log error once
            return ScriptValue.EMPTY_ITEM_STACK;
        }
        if (!stack.isDamageableItem())
            return left;
        Number damage = right.num();
        if (damage == null) {
            ; // TODO log error once
            return left;
        }
        ItemStack newStack = this.operate(stack, damage.intValue());
        return ScriptValue.itemStack(newStack);
    }

    protected abstract ItemStack operate(ItemStack stack, int damage);

    public static abstract class Serializer extends TwoObjectOperatorInstruction.Serializer {
        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            super(constructor);
        }
        @Override protected String getLeftKey() { return "item_stack"; }
        @Override protected String getRightKey() { return "damage"; }
    }

}
