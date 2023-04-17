package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private Tnttag plugin = Tnttag.getInstance();
    private ArenaManager arenaManager = plugin.getArenaManager();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != null) {
            if (arenaManager.playerIsInArena(player)) {
                if (player.getInventory().getItem(event.getHand()).equals(new ItemBuilder(Material.BARRIER).displayName(ChatUtils.colorize(ChatUtils.getRaw("in-game-items.leave"))).build())) {
                    if (arenaManager.playerIsInArena(player)) {
                        arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin = Tnttag.getInstance();
        this.arenaManager = plugin.getArenaManager();
        Player player = event.getPlayer();

        if (arenaManager.playerIsInArena(player)) {
            arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
        }
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (arenaManager.playerIsInArena(player)) {
            arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
        }
    }
}
