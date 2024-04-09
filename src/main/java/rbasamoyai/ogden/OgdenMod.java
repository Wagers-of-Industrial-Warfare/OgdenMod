package rbasamoyai.ogden;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler;
import rbasamoyai.ogden.index.OgdenAmmoGroup;
import rbasamoyai.ogden.index.OgdenBaseGroup;
import rbasamoyai.ogden.index.OgdenEntityTypes;
import rbasamoyai.ogden.index.OgdenFirearmsGroup;
import rbasamoyai.ogden.index.OgdenItems;
import rbasamoyai.ogden.index.OgdenProjectilePropertiesRegistry;
import rbasamoyai.ogden.network.OgdenNetwork;

@Mod(OgdenMod.MOD_ID)
public class OgdenMod {

    public static final String MOD_ID = "ogden";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CreativeModeTab
        BASE_TAB = new OgdenBaseGroup(),
        FIREARMS_TAB = new OgdenFirearmsGroup(),
        AMMO_TAB = new OgdenAmmoGroup();

    public OgdenMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.addListener(this::commonSetup);
        forgeBus.addListener(this::registerResourceListeners);
        forgeBus.addListener(this::onDatapackSync);

        OgdenItems.ITEMS.register(modBus);
        OgdenEntityTypes.ENTITY_TYPES.register(modBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OgdenClient.init(modBus, forgeBus));
    }

    private void commonSetup(final FMLCommonSetupEvent evt) {
        evt.enqueueWork(() -> {
            OgdenNetwork.init();
            OgdenProjectilePropertiesRegistry.init();
        });
    }

    private void registerResourceListeners(final AddReloadListenerEvent evt) {
        evt.addListener(AmmunitionPropertiesHandler.ReloadListener.INSTANCE);
    }

    public void onDatapackSync(final OnDatapackSyncEvent evt) {
        if (evt.getPlayer() == null) {
            MinecraftServer server = evt.getPlayerList().getServer();
            AmmunitionPropertiesHandler.syncToServer(server);
        } else {
            ServerPlayer player = evt.getPlayer();
            AmmunitionPropertiesHandler.syncToPlayer(player);
        }
    }

    public static ResourceLocation resource(String path) { return new ResourceLocation(MOD_ID, path); }

}
