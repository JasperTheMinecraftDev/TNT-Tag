package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.api.ArenaEndingEvent;
import nl.juriantech.tnttag.api.ArenaStartedEvent;
import nl.juriantech.tnttag.api.ArenaStartingEvent;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.objects.PlayerData;
import nl.juriantech.tnttag.objects.Round;
import nl.juriantech.tnttag.runnables.StartRunnable;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final Tnttag plugin;
    public final Arena arena;
    public GameState state = GameState.IDLE;
    public PlayerManager playerManager;
    public ItemManager itemManager;
    public StartRunnable startRunnable;
    public Round round;

    public GameManager(Tnttag plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.playerManager = new PlayerManager(plugin, this);
        this.itemManager = plugin.getItemManager();
    }

    public void start() {
        setGameState(GameState.STARTING);
    }

    public void stop() {
        setGameState(GameState.ENDING);
    }

    public void setGameState(GameState state) {
        if (this.state.equals(GameState.INGAME) && state.equals(GameState.STARTING)) return; //Safety check.
        if (this.state == state) return; //Safety check.

        switch (state) {
            case IDLE:
                this.state = GameState.IDLE;
                break;
            case STARTING:
                //Start the countdown
                ArenaStartingEvent arenaStartingEvent = new ArenaStartingEvent(arena.getName());
                Bukkit.getPluginManager().callEvent(arenaStartingEvent);

                this.state = GameState.STARTING;
                this.startRunnable = new StartRunnable(this);
                this.startRunnable.runTaskTimer(plugin, 20, 20);
                break;
            case INGAME:
                //Start the game
                ArenaStartedEvent arenaStartedEvent = new ArenaStartedEvent(arena.getName());
                Bukkit.getPluginManager().callEvent(arenaStartedEvent);

                if (this.startRunnable != null) this.startRunnable.cancel();
                this.state = GameState.INGAME;
                for (Player player : playerManager.getPlayers().keySet()) {
                    itemManager.giveGameItems(player);
                }
                playerManager.sendStartMessage();
                startRound();
                break;
            case ENDING:
                if (round != null) {
                    round.end();
                }
                //Stop the game
                HashMap<Player, PlayerType> playersCopy = new HashMap<>(playerManager.getPlayers());
                for (Map.Entry<Player, PlayerType> entry : playersCopy.entrySet()) {
                        Player player = entry.getKey();
                        if (entry.getValue() == PlayerType.SURVIVOR) {
                            for (String cmd : Tnttag.configfile.getStringList("arena-finish-commands")) {
                                ConsoleCommandSender console = Bukkit.getConsoleSender();
                                Bukkit.dispatchCommand(console, cmd.replace("%winner%", player.getName()));
                            }

                            PlayerData playerData = new PlayerData(player.getUniqueId());
                            int oldWins = playerData.getWins();
                            playerData.setWins(oldWins + 1);

                            ParticleUtils.Firework(player.getLocation(), 0);
                            playerManager.broadcast(ChatUtils.getRaw("arena.player-win").replace("{player}", player.getName()));
                            playerManager.broadcast(ChatUtils.getRaw("arena.returning-to-lobby"));

                            ArenaEndingEvent arenaEndingEvent = new ArenaEndingEvent(arena.getName(), playerManager.getPlayers(), new ArrayList<Player>(List.of(player)));
                            Bukkit.getPluginManager().callEvent(arenaEndingEvent);
                        }
                        playerManager.removePlayer(player, false);
                    }

                setGameState(GameState.IDLE);
                this.startRunnable = null;
                break;
            default:
                throw new IllegalStateException("Unexpected GameState value received: " + state);
        }
    }

    public void startRound() {
        playerManager.teleportToStart();
        playerManager.pickPlayers();
        this.round = new Round(plugin, this);
        this.round.start();
        playerManager.broadcast(ChatUtils.getRaw("arena.tagger-released"));
    }

    public String getCustomizedState() {
        return ChatUtils.colorize(ChatUtils.getRaw("state." + state.toString().toUpperCase()));
    }
}