package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerInformation;
import nl.juriantech.tnttag.utils.ChatUtils;
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
        itemManager.giveLobbyItems(player);
        players.add(player);
        playerInformationMap.put(player, new PlayerInformation(player));
        ChatUtils.sendMessage(player, "player.joined-lobby");
    }

    public void leaveLobby(Player player){
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
