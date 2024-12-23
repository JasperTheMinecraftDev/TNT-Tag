package nl.juriantech.tnttag.objects;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.api.PlayerLostRoundEvent;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.managers.GameManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class Round {

    private final Tnttag plugin;
    private final GameManager gameManager;
    private int roundDuration;
    public boolean ended = false;

    public Round(Tnttag plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.roundDuration = gameManager.arena.getRoundDuration();
    }
    public void start() {
        boolean teleportToStart = gameManager.playerManager.getPlayerCount() <= Tnttag.configfile.getInt("player-teleport-threshold");
        for (Map.Entry<Player, PlayerType> player : gameManager.playerManager.getPlayers().entrySet()) {
            if (player.getValue().equals(PlayerType.SPECTATOR)) continue;

            player.getKey().playSound(player.getKey().getLocation(), Sound.valueOf(ChatUtils.getRaw("sounds.round-start").toUpperCase()), 1, 1);
            ChatUtils.sendTitle(player.getKey(), "titles.round-start", 20L, 20L, 20L);
            if (teleportToStart) player.getKey().teleport(gameManager.arena.getStartLocation());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                roundDuration--;
                gameManager.getScoreboardManager().update();
                for (Player player : gameManager.playerManager.getPlayers().keySet()) {
                    player.setLevel(Math.max(roundDuration, 0));
                    if (gameManager.playerManager.getPlayers().get(player) == PlayerType.TAGGER) {
                        updateCompass(player);
                        ChatUtils.sendActionBarMessage(player, ChatUtils.getRaw("actionBarMessages.tagger"));
                    } else if (gameManager.playerManager.getPlayers().get(player) == PlayerType.SURVIVOR) {
                        ChatUtils.sendActionBarMessage(player, ChatUtils.getRaw("actionBarMessages.survivor"));
                    } else if (gameManager.playerManager.getPlayers().get(player) == PlayerType.SPECTATOR) {
                        ChatUtils.sendActionBarMessage(player, ChatUtils.getRaw("actionBarMessages.spectator"));
                    }
                }

                if (roundDuration == 0) {
                    cancel();
                    ended = true;
                    end(false);
                    if (gameManager.playerManager.getPlayerCount() == 1) {
                        gameManager.setGameState(GameState.ENDING, false);
                    } else {
                        //Start a new round
                        gameManager.playerManager.broadcast(ChatUtils.getRaw("arena.new-round-starting").replace("%seconds%", String.valueOf(Tnttag.configfile.getInt("delay.new-round") * 20)));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                gameManager.startRound();
                            }
                        }.runTaskLater(plugin, Tnttag.configfile.getInt("delay.new-round") * 20);
                    }
                } else if (roundDuration < 0) {
                    //The game has crashed due to an error
                    cancel();
                    end(false);
                    Bukkit.getLogger().severe("This round is on a crashed state. Something has caused an error and made the round unable to continue. Stopping game...");
                    Bukkit.getLogger().severe("Please report the stacktrace above to our discord!");
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void end(boolean forceWinForTagger) {
        gameManager.playerManager.broadcast(ChatUtils.getRaw("arena.round-ended"));
        for (Map.Entry<Player, PlayerType> entry : gameManager.playerManager.getPlayers().entrySet()) {
            Player player = entry.getKey();

            player.playSound(player.getLocation(), Sound.valueOf(ChatUtils.getRaw("sounds.round-end").toUpperCase()), 1, 1);
            ChatUtils.sendTitle(player, "titles.round-end", 20L, 20L, 20L);
            if (entry.getValue() == PlayerType.SPECTATOR) continue; //This should NOT affect spectators.
            if (entry.getValue() == PlayerType.TAGGER) {
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                // Execute the round command for tnt players:
                for (String cmd : Tnttag.configfile.getStringList("round-finish-commands.taggers")) {
                    Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));
                }

                if (!forceWinForTagger) {
                    PlayerLostRoundEvent event = new PlayerLostRoundEvent(player, gameManager.arena.getName());
                    Bukkit.getPluginManager().callEvent(event);
                    player.getWorld().createExplosion(player.getLocation(), 0.5F, false, false);
                    gameManager.playerManager.broadcast(ChatUtils.getRaw("arena.player-blew-up").replace("{player}", player.getName()));
                    ChatUtils.sendTitle(player, "titles.lose", 20L, 20L, 20L);
                }

                player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
                player.getInventory().setItem(0, new ItemStack(Material.AIR, 1));


                gameManager.playerManager.setPlayerType(player, PlayerType.SPECTATOR);

                if (!forceWinForTagger) ChatUtils.sendMessage(player, "player.lost-game");
                continue;
            }

            //The other people, survivors.
            for (String cmd : Tnttag.configfile.getStringList("round-finish-commands.survivors")) {
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                Bukkit.dispatchCommand(console, cmd.replace("%player%", player.getName()));
            }

            ParticleUtils.Firework(player.getLocation(), 0);
        }
        ended = true;
    }

    public void updateCompass(Player player) {
        ItemStack compass = player.getInventory().getItem(7);
        Player nearestPlayer = getNearestSurvivor(player);

        if (compass != null && nearestPlayer != null && compass.getItemMeta() != null) {
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName((int) player.getLocation().distance(nearestPlayer.getLocation()) + "m");
            compass.setItemMeta(meta);
            player.setCompassTarget(nearestPlayer.getLocation());
        }
    }

    /**
     * Returns the nearest tagger to another player, or null if there is no other player in this world
     * @param player Player to check
     * @return Nearest other tagger, or null if there is no other player in this world
     */
    public Player getNearestSurvivor(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        ArrayList<Player> playersInArena = new ArrayList<>(world.getEntitiesByClass(Player.class));
        if (playersInArena.size() == 1) {
            return null;
        }

        playersInArena.remove(player);
        playersInArena.removeIf(p -> !gameManager.playerManager.getPlayers().containsKey(p));
        playersInArena.removeIf(p -> p != null && !gameManager.playerManager.getPlayers().get(p).equals(PlayerType.SURVIVOR));
        playersInArena.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(location)));
        return playersInArena.isEmpty() ? null : playersInArena.get(0);
    }

    public int getRoundDuration() {
        return roundDuration;
    }
}