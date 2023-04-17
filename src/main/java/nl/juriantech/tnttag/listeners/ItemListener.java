package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

    private final Tnttag plugin = Tnttag.getInstance();
    private final ArenaManager arenaManager = plugin.getArenaManager();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != null) {
            if (arenaManager.playerIsInArena(player)) {
                if (player.getInventory().getItem(event.getHand()).equals(new ItemBuilder(Material.BARRIER).displayName(ChatUtils.colorize(ChatUtils.getRaw("in-game-items.leave"))).build())) {
                    arenaManager.getPlayerArena(player).getGameManager().playerManager.removePlayer(player, true);
                    event.setCancelled(true);
                } else if (player.getInventory().getItem(event.getHand()).equals(new ItemBuilder(Material.DIAMOND_AXE).displayName(ChatUtils.colorize(ChatUtils.getRaw("in-game-items.join"))).build())) {
                    player.performCommand("tt joingui");
                    event.setCancelled(true);
                }
            }
        }
    }
}
