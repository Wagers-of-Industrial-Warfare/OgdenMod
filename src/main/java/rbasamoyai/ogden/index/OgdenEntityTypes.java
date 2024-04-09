package rbasamoyai.ogden.index;

import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.entities.OgdenBullet;

public class OgdenEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registry.ENTITY_TYPE_REGISTRY, OgdenMod.MOD_ID);

    public static final RegistryObject<EntityType<OgdenBullet>> BULLET = ENTITY_TYPES.register("bullet",
            () -> EntityType.Builder.<OgdenBullet>of(OgdenBullet::new, MobCategory.MISC)
                    .sized(0.125f, 0.125f)
                    .clientTrackingRange(16)
                    .setUpdateInterval(1)
                    .fireImmune()
                    .build("bullet"));

}
