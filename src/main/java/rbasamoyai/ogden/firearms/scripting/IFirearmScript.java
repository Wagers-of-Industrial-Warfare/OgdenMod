package rbasamoyai.ogden.firearms.scripting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IFirearmScript {

    void tick(LivingEntity entity, Level level, ItemStack stack);

}
