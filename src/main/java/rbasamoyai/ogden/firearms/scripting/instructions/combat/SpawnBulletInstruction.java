package rbasamoyai.ogden.firearms.scripting.instructions.combat;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.ogden.entities.OgdenBullet;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SpawnBulletInstruction implements ScriptInstruction {

    private final ScriptValueSupplier itemStack;
    private final ScriptValueSupplier spread;
    private final ScriptValueSupplier power;
    private final ScriptValueSupplier spawnAhead;

    public SpawnBulletInstruction(ScriptValueSupplier itemStack, ScriptValueSupplier spread, ScriptValueSupplier power, ScriptValueSupplier spawnAhead) {
        this.itemStack = itemStack;
        this.spread = spread;
        this.power = power;
        this.spawnAhead = spawnAhead;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SPAWN_BULLET; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ItemStack itemStackRes = this.itemStack.run(context).stack();
        if (itemStackRes == null) {
            ;// TODO log error once
            return ScriptValue.VOID;
        }
        Number powerRes = this.power.run(context).num();
        if (powerRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number spreadRes = this.spread.run(context).num();
        if (spreadRes == null) spreadRes = 0f;
        Number aheadRes = this.spawnAhead.run(context).num();
        if (aheadRes == null) aheadRes = 1f;

        LivingEntity living = context.entity();
        Level level = context.level();
        ItemStack firearm = context.stack();
        if (!level.isClientSide) {
            OgdenBullet proj = new OgdenBullet(level, itemStackRes.getItem(), firearm.getItem(), living);
            Vec3 viewVec = living.getViewVector(1).scale(aheadRes.doubleValue());
            proj.setPos(proj.position().add(viewVec));
            proj.shootFromRotation(living, living.getXRot(), living.getYRot(), 0.0f, powerRes.floatValue(), spreadRes.floatValue());
            level.addFreshEntity(proj);
        }
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("bullet"))
                throw new JsonParseException("Spawn bullet instruction missing parameter 'bullet'");
            if (!obj.has("power"))
                throw new JsonParseException("Spawn bullet instruction missing parameter 'power'");
            ScriptValueSupplier bullet = ScriptValueSupplier.fromJson(obj.get("bullet"));
            ScriptValueSupplier spread = obj.has("spread") ? ScriptValueSupplier.fromJson(obj.get("spread")) : ScriptValue.ZERO;
            ScriptValueSupplier power = ScriptValueSupplier.fromJson(obj.get("power"));
            ScriptValueSupplier ahead = obj.has("ahead") ? ScriptValueSupplier.fromJson(obj.get("ahead")) : ScriptValue.ofFloat(1f);
            return new SpawnBulletInstruction(bullet, spread, power, ahead);
        }
    }

}
