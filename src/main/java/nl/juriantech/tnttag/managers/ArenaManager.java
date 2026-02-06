package nl.juriantech.tnttag.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dev.dejvokep.boostedyaml.YamlDocument;
import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ArenaManager {

    private final Tnttag plugin;
    public ArrayList<Arena> arenaObjects = new ArrayList<>();

    public ArenaManager(Tnttag plugin) {
        this.plugin = plugin;
    }

    public Arena getArena(String name) {
        for (Arena arena : this.arenaObjects) {
            if (arena.getName().equals(name))
                return arena;
        }
        return null;
    }

    public void deleteArena(String name) throws IOException {
        if (getArena(name) != null) {
            Arena arena = getArena(name);
            this.arenaObjects.remove(arena);
            Tnttag.arenasfile.remove(arena.getName());
            Tnttag.arenasfile.save();
        }
    }

    public boolean playerIsInArena(Player player) {
        for (Arena arena : this.arenaObjects) {
            if (arena.getGameManager().playerManager.isIn(player))
                return true;
        }
        return false;
    }

    public Arena getPlayerArena(Player player) {
        for (Arena arena : this.arenaObjects) {
            if (arena.getGameManager().playerManager.isIn(player))
                return arena;
        }
        return null;
    }

    public ArrayList<Arena> getArenaObjects() {
        return this.arenaObjects;
    }

    public void saveArenasToFile() {
        int arenas = 0;
        if (this.arenaObjects == null) {
            return;
        }
        for (Arena arena : this.arenaObjects) {
            arenas++;
            saveArenaToFile(arena);
        }
        Bukkit.getLogger().info("Saved " + arenas + " TNT-Tag arena(s)!");
    }

    public void loadArenasFromFile() {
        int arenas = 0;
        YamlDocument arenasFile = Tnttag.arenasfile;
        if (arenasFile == null) {
            Bukkit.getLogger().severe("[Tnt-tag] Error: arenas file is null, could not load arenas.");
            return;
        }
        if (!arenaObjects.isEmpty()) arenaObjects.clear();
        for (String route : arenasFile.getRoutesAsStrings(false)) {
            arenas++;
            String startLocWorldName = arenasFile.getString(route + ".startLocation.world");
            World startLocWorld = Bukkit.getWorld(startLocWorldName);
            if (startLocWorld == null) {
                Bukkit.getLogger().severe("[Tnt-tag] Error: start location world for arena " + route + " is not loaded, disabling the arena...");
                continue;
            }
            double startLocX = arenasFile.getDouble(route + ".startLocation.x");
            double startLocY = arenasFile.getDouble(route + ".startLocation.y");
            double startLocZ = arenasFile.getDouble(route + ".startLocation.z");
            Location startLoc = new Location(startLocWorld, startLocX, startLocY, startLocZ);

            String lobbyLocWorldName = arenasFile.getString(route + ".lobbyLocation.world");
            World lobbyLocWorld = Bukkit.getWorld(lobbyLocWorldName);
            if (lobbyLocWorld == null) {
                Bukkit.getLogger().severe("[Tnt-tag] Error: lobby location world for arena " + route + " is not loaded, disabling the arena...");
                continue;
            }
            double lobbyLocX = arenasFile.getDouble(route + ".lobbyLocation.x");
            double lobbyLocY = arenasFile.getDouble(route + ".lobbyLocation.y");
            double lobbyLocZ = arenasFile.getDouble(route + ".lobbyLocation.z");
            Location lobbyLoc = new Location(lobbyLocWorld, lobbyLocX, lobbyLocY, lobbyLocZ);

            int maxPlayers = arenasFile.getInt(route + ".maxPlayers");
            int minPlayers = arenasFile.getInt(route + ".minPlayers");
            int roundDuration = arenasFile.getInt(route + ".roundDuration");
            int countdown = arenasFile.getInt(route + ".countdown");

            List<String> potionEffects = arenasFile.getStringList(route + ".potionEffects");

            if (getArena(route) == null) {
                Arena arena = new Arena(plugin, route, startLoc, lobbyLoc, maxPlayers, minPlayers, (ArrayList<String>) potionEffects, roundDuration, countdown);
                Bukkit.getLogger().info("Loaded arena " + arena.getName());
                arenaObjects.add(arena);
            }
        }
        Bukkit.getLogger().info("Loaded " + arenas + " TNT-Tag arena(s)!");
    }

    public void saveArenaToFile(Arena arena) {
        YamlDocument arenasFile = Tnttag.arenasfile;
        if (arena == null) {
            return;
        }
        arenasFile.set(arena.getName() + ".startLocation.world", Objects.requireNonNull(arena.getStartLocation().getWorld()).getName());
        arenasFile.set(arena.getName() + ".startLocation.x", arena.getStartLocation().getX());
        arenasFile.set(arena.getName() + ".startLocation.y", arena.getStartLocation().getY());
        arenasFile.set(arena.getName() + ".startLocation.z", arena.getStartLocation().getZ());
        arenasFile.set(arena.getName() + ".lobbyLocation.world", Objects.requireNonNull(arena.getLobbyLocation().getWorld()).getName());
        arenasFile.set(arena.getName() + ".lobbyLocation.x", arena.getLobbyLocation().getX());
        arenasFile.set(arena.getName() + ".lobbyLocation.y", arena.getLobbyLocation().getY());
        arenasFile.set(arena.getName() + ".lobbyLocation.z", arena.getLobbyLocation().getZ());
        arenasFile.set(arena.getName() + ".maxPlayers", arena.getMaxPlayers());
        arenasFile.set(arena.getName() + ".minPlayers", arena.getMinPlayers());
        arenasFile.set(arena.getName() + ".potionEffects", arena.getPotionEffects());
        arenasFile.set(arena.getName() + ".roundDuration", arena.getRoundDuration());
        arenasFile.set(arena.getName() + ".countdown", arena.getCountdown());
        try {
            arenasFile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getLogger().info("Saved TNT-Tag arena " + arena.getName() + "!");
    }

    public int getArenaObjectsSize() {
        return this.arenaObjects.size();
    }

    public void reload() throws IOException {
        Tnttag.arenasfile.reload();
        Tnttag.arenasfile.save();
        this.arenaObjects.clear();
        loadArenasFromFile();
    }

    public void endAllArenas() {
        for (Arena arena : arenaObjects) {
            GameManager manager = arena.getGameManager();
            PlayerManager playerManager = manager.playerManager;
            for (Player player : new HashMap<>(playerManager.getPlayers()).keySet()) {
                playerManager.removePlayer(player, true);
            }
        }
    }
}