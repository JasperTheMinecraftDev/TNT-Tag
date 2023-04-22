package nl.juriantech.tnttag.api;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.objects.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

public class API {

    private static ArenaManager arenaManager = null;

    public API(Tnttag plugin) {
        arenaManager = plugin.getArenaManager();
    }

    public int getTimesTagged(UUID playerUUID) {
        PlayerData data = new PlayerData(playerUUID);
        return data.getTimesTagged();
    }

    public int getWins(UUID playerUUID) {
        PlayerData data = new PlayerData(playerUUID);
        return data.getWins();
    }

    public int getTags(UUID playerUUID) {
        PlayerData data = new PlayerData(playerUUID);
        return data.getTags();
    }

    public void setTimesTagged(UUID playerUUID, int value) {
        PlayerData data = new PlayerData(playerUUID);
        data.setTimesTagged(value);
    }

    public void setWins(UUID playerUUID, int value) {
        PlayerData data = new PlayerData(playerUUID);
        data.setWins(value);
    }

    public void setTags(UUID playerUUID, int value) {
        PlayerData data = new PlayerData(playerUUID);
        data.setTags(value);
    }

    public boolean arenaExists(String arenaName) {
        return arenaManager.getArena(arenaName) != null;
    }

    public HashMap<Player, PlayerType> getPlayers(String arenaName) {
        if (arenaExists(arenaName)) {
            return arenaManager.getArena(arenaName).getGameManager().playerManager.getPlayers();
        }
        return null;
    }

    public String getArenaState(String arenaName) {
        if (arenaExists(arenaName)) {
            return arenaManager.getArena(arenaName).getGameManager().getCustomizedState();
        }
        return "Unknown arena";
    }

    public TreeMap<UUID, Integer> getWinsData() {
        TreeMap<UUID, Integer> winsData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".wins") != null) {
                int wins = Tnttag.playerdatafile.getInt(route + ".wins");
                winsData.put(UUID.fromString(route), wins);
            }
        }
        return winsData;
    }

    public TreeMap<UUID, Integer> getTimesTaggedData() {
        TreeMap<UUID, Integer> timesTaggedData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".timestagged") != null) {
                int kills = Tnttag.playerdatafile.getInt(route + ".timestagged");
                timesTaggedData.put(UUID.fromString(route), kills);
            }
        }
        return timesTaggedData;
    }

    public TreeMap<UUID, Integer> getTagsData() {
        TreeMap<UUID, Integer> tagsData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".tags") != null) {
                int deaths = Tnttag.playerdatafile.getInt(route + ".tags");
                tagsData.put(UUID.fromString(route), deaths);
            }
        }
        return tagsData;
    }
}
