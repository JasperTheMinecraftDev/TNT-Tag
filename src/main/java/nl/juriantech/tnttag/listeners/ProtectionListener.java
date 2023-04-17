package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class ProtectionListener implements Listener {

    private final Tnttag plugin = Tnttag.getInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().playerIsInArena(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (plugin.getArenaManager().playerIsInArena(player)) {
            event.setCancelled(true);
        }
    }
}
