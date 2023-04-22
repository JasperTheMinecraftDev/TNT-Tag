package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

public class EntityDamageByEntityListener implements Listener {

    private final Tnttag plugin;

    public EntityDamageByEntityListener(Tnttag plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        Arena damagerArena = plugin.getArenaManager().getPlayerArena(damager);
        Arena victimArena = plugin.getArenaManager().getPlayerArena(victim);
        if (victimArena == null || damagerArena == null || !victimArena.getName().equals(damagerArena.getName())) {
            return;
        }
        event.setCancelled(true);

        for (Map.Entry<Player, PlayerType> entry : damagerArena.getGameManager().playerManager.getPlayers().entrySet()) {
            if (entry.getValue() == PlayerType.TAGGER && entry.getKey().getName().equals(damager.getName())) {
                damagerArena.getGameManager().playerManager.setPlayerType(damager, PlayerType.SURVIVOR);
                damagerArena.getGameManager().playerManager.setPlayerType(victim, PlayerType.TAGGER);
                ChatUtils.sendCustomMessage(victim, ChatUtils.getRaw("player.tagged").replace("{tagger}", damager.getName()));
            }
        }
    }
}