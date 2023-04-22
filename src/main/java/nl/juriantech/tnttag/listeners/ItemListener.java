package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.managers.LobbyManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

    private final ArenaManager arenaManager;
    private final LobbyManager lobbyManager;

    public ItemListener(Tnttag plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.lobbyManager = plugin.getLobbyManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != null) {
            if (lobbyManager.playerIsInLobby(player)) {
                if (player.getInventory().getItem(event.getHand()).equals(ItemBuilder.from(ChatUtils.getRaw("items.leave")).build())) {
                    lobbyManager.leaveLobby(player);
                    if (arenaManager.playerIsInArena(player)) {
                        arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
                    }
                    event.setCancelled(true);
                } else if (player.getInventory().getItem(event.getHand()).equals(ItemBuilder.from(ChatUtils.getRaw("items.join")).build())) {
                    player.performCommand("tt joingui");
                    event.setCancelled(true);
                }
            }
        }
    }
}
