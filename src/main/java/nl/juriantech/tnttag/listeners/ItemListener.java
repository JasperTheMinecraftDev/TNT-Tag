package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.managers.LobbyManager;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

    private final ArenaManager arenaManager;
    private final LobbyManager lobbyManager;
    private final SignManager signManager;

    public ItemListener(Tnttag plugin) {
        this.arenaManager = plugin.getArenaManager();
        this.lobbyManager = plugin.getLobbyManager();
        this.signManager = plugin.getSignManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == null) return;

        Player player = event.getPlayer();
        if (!lobbyManager.playerIsInLobby(player)) return;

        if (player.getInventory().getItem(event.getHand()).equals(ItemBuilder.from(ChatUtils.getRaw("items.leave")).build())) {
            //You need to do this otherwise if you click a plugin sign with quick inventory slot 9 selected the player will join and immediately leave
            if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign)  {
                if (signManager.isPluginSign((Sign) event.getClickedBlock().getState())) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (arenaManager.playerIsInArena(player)) {
                arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
            }
            lobbyManager.leaveLobby(player);
            event.setCancelled(true);
        } else if (player.getInventory().getItem(event.getHand()).equals(ItemBuilder.from(ChatUtils.getRaw("items.join")).build())) {
            if (Tnttag.configfile.getBoolean("open-arena-gui-on-join")) {
                player.performCommand("tt joingui");
            }
            event.setCancelled(true);
        }
    }
}