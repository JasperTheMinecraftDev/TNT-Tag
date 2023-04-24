package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerInformation;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class LobbyManager {

    private final ItemManager itemManager;
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final HashMap<Player, PlayerInformation> playerInformationMap;

    public LobbyManager(Tnttag plugin) {
        this.itemManager = plugin.getItemManager();
        this.playerInformationMap = new HashMap<>();
    }

    public void enterLobby(Player player) {
        if (Tnttag.configfile.getString("globalLobby") != null) {
            ChatUtils.sendMessage(player, "player.global-lobby-not-set");
            return;
        }
        String[] parts = Tnttag.configfile.getString("globalLobby").split(",");
        World world = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        player.teleport(new Location(world, x, y, z));
        players.add(player);
        // This should be done first because the PlayerInformation constructor clears the inventory too.
        playerInformationMap.put(player, new PlayerInformation(player));
        itemManager.giveLobbyItems(player);
        ChatUtils.sendMessage(player, "player.joined-lobby");
    }

    public void leaveLobby(Player player) {
        itemManager.clearInv(player);
        players.remove(player);
        PlayerInformation playerInfo = playerInformationMap.remove(player);
        if (playerInfo != null) {
            playerInfo.restore();
        }
        ChatUtils.sendMessage(player, "player.leaved-lobby");
    }

    public boolean playerIsInLobby(Player player) {
        return players.contains(player);
    }
}
