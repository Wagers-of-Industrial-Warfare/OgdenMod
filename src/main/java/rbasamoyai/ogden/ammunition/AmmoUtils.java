package rbasamoyai.ogden.ammunition;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import rbasamoyai.ogden.firearms.config.FirearmAmmoPredicate;

public class AmmoUtils {

    public static int countAvailableAmmo(Container container, FirearmAmmoPredicate predicate) {
        int count = 0;
        int sz = container.getContainerSize();
        for (int i = 0 ; i < sz; ++i)
            count += predicate.countAmmo(container.getItem(i));
        return count;
    }

    public static int countAvailableClips(Container container, FirearmAmmoPredicate predicate) {
        int count = 0;
        int sz = container.getContainerSize();
        for (int i = 0; i < sz; ++i) {
            if (predicate.testClip(container.getItem(i)))
                ++count;
        }
        return count;
    }

    private static final Container EMPTY_CONTAINER = new SimpleContainer(1);

    // TODO add registry instead of hardcoding
    public static Container getEntityInventory(LivingEntity entity) {
        if (entity instanceof Player player)
            return player.getInventory();
        return EMPTY_CONTAINER;
    }

    private AmmoUtils() {
    }

}
