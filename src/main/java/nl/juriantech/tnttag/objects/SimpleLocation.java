package nl.juriantech.tnttag.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SimpleLocation {
    private final World world;
    private final double x;
    private final double y;
    private final double z;

    public SimpleLocation(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void teleport(Player player) {
        Location location = toLocation();
        if (location != null) {
            player.teleport(location);
        }
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    @Override
    public String toString() {
        return world.getName() + ":" + x + ":" + y + ":" + z;
    }

    public static SimpleLocation fromString(String str) {
        String[] parts = str.split(":");
        if (parts.length == 4) {
            World world = Bukkit.getWorld(parts[0]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            return new SimpleLocation(world, x, y, z);
        }
        return null;
    }

    public static SimpleLocation fromLocation(Location loc) {
        return new SimpleLocation(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }
}
