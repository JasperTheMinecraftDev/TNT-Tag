package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.managers.ArenaManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ProtectionListener implements Listener {

    private final Tnttag plugin;

    public ProtectionListener(Tnttag plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.getLobbyManager().playerIsInLobby(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (plugin.getLobbyManager().playerIsInLobby(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (plugin.getLobbyManager().playerIsInLobby(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ArenaManager arenaManager = plugin.getArenaManager();

            boolean isInActiveArena = false;
            boolean playerInArena = arenaManager.playerIsInArena(player);

            if (playerInArena) {
                Arena arena = arenaManager.getPlayerArena(player);

                if (arena != null && arena.getGameManager().state == GameState.INGAME) {
                    isInActiveArena = true;
                }
            }

            if (plugin.getLobbyManager().playerIsInLobby(player) && !isInActiveArena) {
                event.setCancelled(true);
            } else if (isInActiveArena && !isDamageFromPlayer(event, player)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isDamageFromPlayer(EntityDamageEvent event, Player player) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityEvent = (EntityDamageByEntityEvent) event;
            Entity damager = entityEvent.getDamager();

            if (damager instanceof Player) {
                Player attacker = (Player) damager;
                ArenaManager arenaManager = plugin.getArenaManager();
                Arena playerArena = arenaManager.getPlayerArena(player);
                Arena attackerArena = arenaManager.getPlayerArena(attacker);

                return playerArena != null && playerArena.equals(attackerArena);
            }
        }

        return false;
    }
}
