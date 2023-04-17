package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignManager {

    private final Tnttag plugin;
    private final Map<String, List<Location>> signLocations;

    public SignManager(Tnttag plugin) {
        this.plugin = plugin;
        this.signLocations = new HashMap<>();
    }

    public void addSign(String arenaName, Location location) {
        if (signLocations.containsKey(arenaName)) {
            signLocations.get(arenaName).add(location);
            saveSigns();
        } else {
            List<Location> locations = new ArrayList<>();
            locations.add(location);
            signLocations.put(arenaName, locations);
            saveSigns();
        }
    }

    public void removeSign(String arenaName, Location location) {
        if (signLocations.containsKey(arenaName)) {
            List<Location> locations = signLocations.get(arenaName);
            locations.remove(location);
            if (locations.isEmpty()) {
                signLocations.remove(arenaName);
                saveSigns();
            }
        }
    }

    public boolean isTNTTagSign(Location location) {
        for (Map.Entry<String, List<Location>> entry : signLocations.entrySet()) {
            if (entry.getValue().contains(location)) {
                return true;
            }
        }
        return false;
    }

    public void loadSigns() {
        for (String arenaName : Tnttag.signsdatafile.getRoutesAsStrings(false)) {
            List<Location> locations = new ArrayList<>();
            List<String> serializedLocations = Tnttag.signsdatafile.getStringList(arenaName);

            for (String serializedLocation : serializedLocations) {
                Location location = deserializeLocation(serializedLocation);
                locations.add(location);
            }

            signLocations.put(arenaName, locations);
        }
    }

    public void saveSigns() {
        for (String arenaName : signLocations.keySet()) {
            List<Location> locations = signLocations.get(arenaName);
            List<String> serializedLocations = new ArrayList<>();

            for (Location location : locations) {
                String serializedLocation = serializeLocation(location);
                serializedLocations.add(serializedLocation);
            }

            Tnttag.signsdatafile.set(arenaName, serializedLocations);
        }

        try {
            Tnttag.signsdatafile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSigns(String arenaName) {
        List<Location> locations = signLocations.get(arenaName);
        if (locations == null) {
            return;
        }
        for (Location location : locations) {
            if (location != null) {
                Block block = location.getBlock();
                if (block.getState() instanceof Sign) {
                    Sign sign = (Sign) block.getState();
                    Arena arena = plugin.getArenaManager().getArena(arenaName);
                    if (arena == null) {
                        return;
                    }
                    int currentPlayers = arena.getGameManager().playerManager.getPlayerCount();
                    int maxPlayers = arena.getMaxPlayers();
                    sign.setLine(0, ChatColor.GOLD + "[" + ChatColor.RED + "Tnttag" + ChatColor.GOLD + "]");
                    sign.setLine(1, ChatColor.WHITE + arenaName);
                    sign.setLine(2, arena.getGameManager().getCustomizedState() + ChatColor.RESET + ": " + ChatColor.GOLD + ChatColor.BOLD + currentPlayers
                            + ChatColor.YELLOW + "/" + ChatColor.WHITE + ChatColor.BOLD + maxPlayers);
                    sign.update(true);
                }
            } else {
                plugin.getLogger().severe("One of your signs is corrupted, please reset your signs.yml file and replace your signs.");
            }
        }
    }

    public void updateAllSigns() {
        for (String arenaName : signLocations.keySet()) {
            updateSigns(arenaName);
        }
    }

    private String serializeLocation(Location location) {
        if (location.getWorld() == null) {
            plugin.getLogger().severe("We tried to load a sign but it failed because the world was null.");
        }
        return location.getWorld().getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ();
    }

    private Location deserializeLocation(String serializedLocation) {
        String[] parts = serializedLocation.split(",");
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}