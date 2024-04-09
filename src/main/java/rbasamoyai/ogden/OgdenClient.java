package rbasamoyai.ogden;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import rbasamoyai.ogden.entities.OgdenBulletRenderer;
import rbasamoyai.ogden.index.OgdenEntityTypes;

public class OgdenClient {

    public static void init(IEventBus modBus, IEventBus forgeBus) {
        modBus.addListener(OgdenClient::setupRenderers);
    }

    public static void setupRenderers(final EntityRenderersEvent.RegisterRenderers evt) {
        evt.registerEntityRenderer(OgdenEntityTypes.BULLET.get(), OgdenBulletRenderer::new);
    }

}
