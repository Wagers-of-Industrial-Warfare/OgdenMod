package rbasamoyai.ogden.firearms.scripting;

import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rbasamoyai.ogden.firearms.OgdenFirearmItem;

public record FirearmScript(String equipState, Map<String, FirearmState> states) implements IFirearmScript {

    public void tick(LivingEntity entity, Level level, ItemStack stack) {
        if (!(stack.getItem() instanceof OgdenFirearmItem firearm)) { // This should not happen!
            ; // TODO log invalid item error once
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("CurrentState", CompoundTag.TAG_STRING))
            tag.putString("CurrentState", this.equipState);
        String currentState = tag.getString("CurrentState");
        if (!this.states.containsKey(currentState)) {
            ; // TODO log invalid state error once
            if (currentState.equals(this.equipState)) { // This should not happen!
                ; // TODO log no valid equip state error
            }
            return;
        }

        FirearmState state = this.states.get(currentState);
        state.tick(new ScriptContext(entity, level, stack, state));

        String newState = tag.getString("CurrentState");
        if (!newState.equals(currentState)) {
            if (!this.states.containsKey(newState)) {
                ; // TODO log invalid state change error
                tag.putString("CurrentState", this.equipState);
                return;
            }
        }
    }

}
