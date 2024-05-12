package rbasamoyai.ogden;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import rbasamoyai.ogden.entities.OgdenBulletRenderer;
import rbasamoyai.ogden.index.OgdenEntityTypes;

public class OgdenClient {

    public static void init(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(OgdenClient::setupRenderers);

        forgeBus.addListener(OgdenClient::onInput);
    }

    public static void setupRenderers(final EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(OgdenEntityTypes.BULLET.get(), OgdenBulletRenderer::new);
    }

    public static void onInput(final InputEvent evt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack stack = mc.player.getUseItem();
        InteractionHand hand = mc.player.getUsedItemHand();
    }

}
