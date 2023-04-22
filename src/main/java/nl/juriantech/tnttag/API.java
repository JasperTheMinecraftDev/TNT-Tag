package nl.juriantech.tnttag;

import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.objects.PlayerData;
import org.bukkit.entity.Player;

import java.util.HashMap;
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
}
