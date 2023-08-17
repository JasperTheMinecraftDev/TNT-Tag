package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (Tnttag.configfile.getBoolean("bungee-mode.enabled")) player.performCommand("tt join");
    }
}
