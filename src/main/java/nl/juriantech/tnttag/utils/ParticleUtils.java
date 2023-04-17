package nl.juriantech.tnttag.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.BlockIterator;

public class ParticleUtils {

    public static void Firework(Location loc, int power) {
        World world = loc.getWorld();
        BlockIterator bi = new BlockIterator(loc, 0, 2);
        Location blocktoadd;
        while (bi.hasNext()) {
            blocktoadd = bi.next().getLocation();
            if (blocktoadd.getBlock().getType() != Material.AIR) {
                break;
            }
            Firework firework = (Firework) world.spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta fireworkMeta = firework.getFireworkMeta();
            fireworkMeta.setPower(power);
            firework.setFireworkMeta(fireworkMeta);
        }
    }
}
