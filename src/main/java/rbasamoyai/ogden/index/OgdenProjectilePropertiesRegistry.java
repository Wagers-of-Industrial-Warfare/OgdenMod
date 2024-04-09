package rbasamoyai.ogden.index;

import net.minecraft.world.item.Item;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler;
import rbasamoyai.ogden.entities.OgdenBulletProperties;

public class OgdenProjectilePropertiesRegistry {

    public static void init() {
        registerBulletProperties(OgdenItems.RIFLE_CARTRIDGE.get());
        registerBulletProperties(OgdenItems.BLACK_POWDER_RIFLE_CARTRIDGE.get());
    }

    private static void registerBulletProperties(Item item) {
        AmmunitionPropertiesHandler.registerSerializer(OgdenEntityTypes.BULLET.get(), item, new OgdenBulletProperties.Serializer());
    }

}
