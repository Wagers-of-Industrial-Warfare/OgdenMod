package rbasamoyai.ogden.index;

import java.util.function.UnaryOperator;

import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rbasamoyai.ogden.OgdenMod;

public class OgdenItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registry.ITEM_REGISTRY, OgdenMod.MOD_ID);

    public static final RegistryObject<Item> RIFLE_CARTRIDGE = simpleItem("rifle_cartridge", p -> p.tab(OgdenMod.AMMO_TAB));
    public static final RegistryObject<Item> BLACK_POWDER_RIFLE_CARTRIDGE = simpleItem("black_powder_rifle_cartridge", p -> p.tab(OgdenMod.AMMO_TAB));

    private static RegistryObject<Item> simpleItem(String id, UnaryOperator<Item.Properties> op) {
        return ITEMS.register(id, () -> new Item(op.apply(new Item.Properties())));
    }

    private static RegistryObject<Item> simpleItem(String id) { return simpleItem(id, UnaryOperator.identity()); }

}
