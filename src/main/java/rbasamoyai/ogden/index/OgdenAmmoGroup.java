package rbasamoyai.ogden.index;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class OgdenAmmoGroup extends CreativeModeTab {

    public OgdenAmmoGroup() { super("ogden_ammo"); }

    @Override public ItemStack makeIcon() { return new ItemStack(OgdenItems.RIFLE_CARTRIDGE.get()); }

}
