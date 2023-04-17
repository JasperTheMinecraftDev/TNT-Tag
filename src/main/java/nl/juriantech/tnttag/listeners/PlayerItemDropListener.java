package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerItemDropListener implements Listener {

    private final Tnttag plugin = Tnttag.getInstance();
    ArenaManager arenaManager =  plugin.getArenaManager();

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (arenaManager.playerIsInArena(player)) {
            event.setCancelled(true);
        }
    }
}
