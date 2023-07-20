package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.managers.GameManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Arena damagerArena = plugin.getArenaManager().getPlayerArena(damager);
        Arena victimArena = plugin.getArenaManager().getPlayerArena(victim);

        if (victimArena == null || damagerArena == null || !victimArena.getName().equals(damagerArena.getName())) {
            return;
        }

        GameManager gameManager = damagerArena.getGameManager();

        if (gameManager.state == GameState.INGAME) {
            PlayerType damagerType = gameManager.playerManager.getPlayerType(damager);

            // Check if the damager is not a tagger
            if (damagerType != PlayerType.TAGGER) {
                if (!Tnttag.configfile.getBoolean("game-combat")) {
                    event.setCancelled(true);
                } else if (gameManager.isRunning()) {
                    event.setDamage(0);
                }
                return;
            }

            PlayerType victimType = gameManager.playerManager.getPlayerType(victim);
            if (victimType == PlayerType.TAGGER) {
                return;
            }

            event.setDamage(0);

            // Switch player types if the damager is a tagger and the victim is a survivor
            gameManager.playerManager.setPlayerType(damager, PlayerType.SURVIVOR);
            gameManager.playerManager.setPlayerType(victim, PlayerType.TAGGER);

            ChatUtils.sendCustomMessage(victim, ChatUtils.getRaw("player.tagged").replace("{tagger}", damager.getName()));

            victim.playSound(victim.getLocation(), Sound.valueOf(ChatUtils.getRaw("sounds.tagged").toUpperCase()), 1, 1);
            damager.playSound(damager.getLocation(), Sound.valueOf(ChatUtils.getRaw("sounds.untagged").toUpperCase()), 1, 1);
        }
    }

}