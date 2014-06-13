package com.github.Gamecube762.Sonar;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gamecube762 on 5/16/14.
 */
public class Utils {

    public static boolean areMonstersNearby(Player player, double radius) {
        for (Entity entity : player.getNearbyEntities(radius, radius, radius))
            if (isMonster(entity))
                return true;

        return false;
    }

    public static boolean isMonster(Entity entity) {
        List<EntityType> hostileEntities = new ArrayList<EntityType>();
        hostileEntities.add(EntityType.GHAST);
        hostileEntities.add(EntityType.MAGMA_CUBE);
        hostileEntities.add(EntityType.SLIME);

        return hostileEntities.contains(entity.getType()) || (entity instanceof Monster);
    }

    public static Location getEntityCenter(LivingEntity entity) {
        double
                a = entity.getLocation().getY() - entity.getEyeLocation().getY(),
                b = a/2;
        Location l = entity.getLocation();
        l.setY(l.getY() + b);
        return l;
    }

    public static Location rescale(Location head, Location entity, double viewDistance) {
        Vector difference = entity.subtract(head).toVector();
        difference.normalize().multiply(viewDistance);

        return head.clone().add(difference);
    }

}
