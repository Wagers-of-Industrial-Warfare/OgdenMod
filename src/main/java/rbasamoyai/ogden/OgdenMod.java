package rbasamoyai.ogden;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.living.LivingGetProjectileEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler;
import rbasamoyai.ogden.ammunition.clips.ClipPropertiesHandler;
import rbasamoyai.ogden.firearms.OgdenFirearmItem;
import rbasamoyai.ogden.firearms.config.FirearmAcceptedAmmunitionHandler;
import rbasamoyai.ogden.firearms.config.FirearmAmmoPredicate;
import rbasamoyai.ogden.firearms.scripting.FirearmScriptHandler;
import rbasamoyai.ogden.index.OgdenAmmoGroup;
import rbasamoyai.ogden.index.OgdenBaseGroup;
import rbasamoyai.ogden.index.OgdenEntityTypes;
import rbasamoyai.ogden.index.OgdenFirearmsGroup;
import rbasamoyai.ogden.index.OgdenItems;
import rbasamoyai.ogden.index.OgdenProjectilePropertiesRegistry;
import rbasamoyai.ogden.index.OgdenRegistries;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;
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

        modBus.addListener(this::onNewRegistry);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::onLoadConfig);
        modBus.addListener(this::onReloadConfig);

        forgeBus.addListener(this::registerResourceListeners);
        forgeBus.addListener(this::onDatapackSync);
        forgeBus.addListener(this::onLoadLevel);
        forgeBus.addListener(this::getProjectileItem);

        OgdenConfigs.registerConfigs();

        OgdenItems.ITEMS.register(modBus);
        OgdenEntityTypes.ENTITY_TYPES.register(modBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> OgdenClient.init(modBus, forgeBus));
    }

    private void onNewRegistry(final NewRegistryEvent evt) {
        OgdenRegistries.initRegistries();

        OgdenScriptInstructionTypes.register();
    }

    private void commonSetup(final FMLCommonSetupEvent evt) {
        evt.enqueueWork(() -> {
            OgdenNetwork.init();
            OgdenProjectilePropertiesRegistry.init();
        });
    }

    private void registerResourceListeners(final AddReloadListenerEvent evt) {
        evt.addListener(AmmunitionPropertiesHandler.ReloadListener.INSTANCE);
        evt.addListener(ClipPropertiesHandler.AcceptedAmmoReloadListener.INSTANCE);
        evt.addListener(ClipPropertiesHandler.CapacityReloadListener.INSTANCE);
        evt.addListener(FirearmAcceptedAmmunitionHandler.ReloadListener.INSTANCE);
        evt.addListener(FirearmScriptHandler.ReloadListener.INSTANCE);}

    public void loadTags() {
        ClipPropertiesHandler.loadTags();
        FirearmAcceptedAmmunitionHandler.loadTags();
    }

    public void onDatapackSync(final OnDatapackSyncEvent evt) {
        if (evt.getPlayer() == null) {
            this.loadTags();

            MinecraftServer server = evt.getPlayerList().getServer();
            AmmunitionPropertiesHandler.syncToAll(server);
            ClipPropertiesHandler.syncToAll(server);
            FirearmAcceptedAmmunitionHandler.syncToAll(server);
        } else {
            ServerPlayer player = evt.getPlayer();
            AmmunitionPropertiesHandler.syncToPlayer(player);
            ClipPropertiesHandler.syncToPlayer(player);
            FirearmAcceptedAmmunitionHandler.syncToPlayer(player);
        }
    }

    public void onLoadLevel(final WorldEvent.Load evt) {
        LevelAccessor level = evt.getWorld();
        if (level.getServer() != null && !level.isClientSide() && level.getServer().overworld() == level) {
            this.loadTags();
        }
    }

    public void onLoadConfig(final ModConfigEvent.Loading evt) {
        OgdenConfigs.onModConfigLoad(evt.getConfig());
    }

    public void onReloadConfig(final ModConfigEvent.Reloading evt) {
        OgdenConfigs.onModConfigReload(evt.getConfig());
    }

    public void getProjectileItem(final LivingGetProjectileEvent evt) {
        ItemStack shootable = evt.getProjectileWeaponItemStack();
        LivingEntity entity = evt.getEntityLiving();
        ItemStack projectile = evt.getProjectileItemStack();
        if (shootable.getItem() instanceof OgdenFirearmItem firearm) {
            FirearmAmmoPredicate predicate = firearm.getFirearmPredicate();
            if (predicate.test(projectile))
                return;
            ItemStack creativeStack = predicate.getCreativeItemStack();
            // TODO registry
            if (entity instanceof Monster monster) {
                evt.setProjectileItemStack(creativeStack);
                return;
            }
            if (entity instanceof Player player) {
                evt.setProjectileItemStack(player.isCreative() ? creativeStack : ItemStack.EMPTY);
                return;
            }
        }
    }

    public static ResourceLocation resource(String path) { return new ResourceLocation(MOD_ID, path); }

}
