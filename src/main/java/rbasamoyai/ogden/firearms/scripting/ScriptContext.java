package rbasamoyai.ogden.firearms.scripting;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ScriptContext(LivingEntity entity, Level level, ItemStack stack, Map<String, ScriptValue> variables, Map<String, ScriptValue> currentFrame,
                            FirearmState state) {

    public ScriptContext(LivingEntity entity, Level level, ItemStack stack, FirearmState state) {
        this(entity, level, stack, new HashMap<>(), new HashMap<>(), state);
    }

    public ScriptValue getVariable(String id) {
        if (this.currentFrame.containsKey(id))
            return this.currentFrame.get(id);
        return this.variables.getOrDefault(id, ScriptValue.VOID);
    }

    public ScriptValue setVariable(String id, ScriptValue value) {
        if (value.isVoid())
            return ScriptValue.VOID;
        this.currentFrame.put(id, value);
        return value;
    }

    public ScriptContext pushLocalFrame() {
        Map<String, ScriptValue> pushFrame = new HashMap<>(this.variables);
        pushFrame.putAll(this.currentFrame);
        return new ScriptContext(this.entity, this.level, this.stack, pushFrame, new HashMap<>(), this.state);
    }

}
