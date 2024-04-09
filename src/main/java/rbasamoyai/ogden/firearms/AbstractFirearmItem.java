package rbasamoyai.ogden.firearms;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import rbasamoyai.ogden.firearms.config.FirearmAcceptedAmmunitionHandler;

public abstract class AbstractFirearmItem extends ProjectileWeaponItem {

    protected AbstractFirearmItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getDefaultProjectileRange() {
        return 0; // TODO data pack config -- "ai_projectile_range"
    }

    @Override public Predicate<ItemStack> getAllSupportedProjectiles() { return FirearmAcceptedAmmunitionHandler.getPredicate(this); }

}
