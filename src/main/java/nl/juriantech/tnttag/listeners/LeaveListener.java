package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private final ArenaManager arenaManager;

    public LeaveListener(Tnttag plugin) {
        this.arenaManager = plugin.getArenaManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
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
