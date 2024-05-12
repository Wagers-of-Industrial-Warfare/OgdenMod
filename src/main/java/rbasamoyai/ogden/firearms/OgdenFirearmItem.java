package rbasamoyai.ogden.firearms;

import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import rbasamoyai.ogden.firearms.config.FirearmAcceptedAmmunitionHandler;
import rbasamoyai.ogden.firearms.config.FirearmAmmoPredicate;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;

public class OgdenFirearmItem extends ProjectileWeaponItem {

    public OgdenFirearmItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0; // TODO data pack config -- "ai_projectile_range"
    }

    @Override public int getUseDuration(ItemStack pStack) { return 0; }

    @Override public Predicate<ItemStack> getAllSupportedProjectiles() { return this.getFirearmPredicate(); }

    public FirearmAmmoPredicate getFirearmPredicate() {
        return FirearmAcceptedAmmunitionHandler.getAmmoPredicate(this);
    }

    public float getPassedActionTime(ScriptContext context) {
        return 1f;
    }

    public void syncFirearmAnimation(ScriptValue animationData, ScriptContext context) {

    }

    public ItemStack getCreativeAmmo(LivingEntity entity, ItemStack weapon) {
        return this.getFirearmPredicate().getCreativeItemStack();
    }

}
