package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.objects.PlayerData;
import nl.juriantech.tnttag.objects.PlayerInformation;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerManager {

    private final Tnttag plugin;
    private final GameManager gameManager;
    private HashMap<Player, PlayerType> players;
    private HashMap<Player, PlayerInformation> playerInformationMap;
    private final Random random = new Random();

    public PlayerManager(Tnttag plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.players = new HashMap<>();
        this.playerInformationMap = new HashMap<>();
    }

    public synchronized void addPlayer(Player player) {
        int minPlayers = gameManager.arena.getMinPlayers();
        int maxPlayers = gameManager.arena.getMaxPlayers();

        if (gameManager.state == GameState.INGAME || getPlayerCount() == maxPlayers) return; //Safety check.
        if (players.containsKey(player)) return; //Safety check.

        players.put(player, PlayerType.WAITING);
        playerInformationMap.put(player, new PlayerInformation(player));
        gameManager.itemManager.giveLobbyItems(player);
        teleportToLobby(player);
        ChatUtils.sendMessage(player, "player.joined-arena");
        broadcast(ChatUtils.getRaw("arena.player-joined").replace("{player}", player.getName()));

        if (getPlayerCount() >= minPlayers) {
            gameManager.start();
        }
    }

    public synchronized void removePlayer(Player player, boolean message) {
        setPlayerType(player, PlayerType.WAITING);
        players.remove(player);
        PlayerInformation playerInfo = playerInformationMap.remove(player);
        if (playerInfo != null) {
            playerInfo.restore();
        }
        if (message) {
            ChatUtils.sendMessage(player, "player.leaved-arena");
            broadcast(ChatUtils.getRaw("arena.player-leaved").replace("{player}", player.getName()));
        }

        if (gameManager.startRunnable != null && !gameManager.startRunnable.isCancelled() && players.size() < gameManager.arena.getMinPlayers()) {
            gameManager.startRunnable.cancel();
            broadcast(ChatUtils.getRaw("arena.countdown-stopped").replace("{player}", player.getName()));
            gameManager.setGameState(GameState.IDLE);
            return;
        }

        if (players.size() == 1 && gameManager.state == GameState.INGAME) {
            if (message) {
                broadcast(ChatUtils.getRaw("arena.last-player-leaved").replace("{player}", player.getName()));
            }
            gameManager.stop();
        }
    }

    public boolean isIn(Player player) {
        for (Player p : players.keySet()) {
            if (p.getName().equals(player.getName())) return true;
        }
        return false;
    }

    public void setPlayerType(Player player, PlayerType type) {
        if (!players.containsKey(player)) return; //Safety check.
        if (players.get(player) == type) return; //Safety check.
        //If the player was a spectator before.
        if (players.get(player) == PlayerType.SPECTATOR) {
            // Make the player visible again
            for (Player p : players.keySet()) {
                p.showPlayer(plugin, player);
            }
            player.setGameMode(GameMode.SURVIVAL);
            player.setFlying(false);
            player.setAllowFlight(false);
        }

        //If the player was a tagger before.
        if (players.get(player).equals(PlayerType.TAGGER)) {
            givePotionEffects(player);
            gameManager.itemManager.giveGameItems(player);
            ChatUtils.sendMessage(player, "player.tagger-removed");
            ChatUtils.sendTitle(player, "titles.untagged", 20L, 20L, 20L);

            PlayerData playerData = new PlayerData(player.getUniqueId());
            playerData.setTags(playerData.getTags() + 1);
        }

        switch (type) {
            case WAITING:
                setType(player, PlayerType.WAITING);
                player.teleport(gameManager.arena.getLobbyLocation());
                break;
            case SURVIVOR:
                setType(player, PlayerType.SURVIVOR);
                player.teleport(gameManager.arena.getStartLocation());
                break;
            case TAGGER:
                if (players.get(player) != PlayerType.SURVIVOR) return; //Safety check.
                setType(player, PlayerType.TAGGER);

                givePotionEffects(player);
                gameManager.itemManager.giveTaggerItems(player);
                ChatUtils.sendTitle(player, "titles.tagged", 20L, 20L, 20L);

                PlayerData playerData = new PlayerData(player.getUniqueId());
                playerData.setTimesTagged(playerData.getTimesTagged() + 1);
                break;
            case SPECTATOR:
                // The player should be invisible.
                for (Player p : players.keySet()) {
                    p.hidePlayer(plugin, player);
                }
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);

                setType(player, PlayerType.SPECTATOR);
                break;
        }
    }

    public void broadcast(String message) {
        message = message.replace("{currentPlayers}", String.valueOf(players.size()));
        message = message.replace("{minPlayers}", String.valueOf(gameManager.arena.getMinPlayers()));
        message = message.replace("{maxPlayers}", String.valueOf(gameManager.arena.getMaxPlayers()));

        for (Player player : players.keySet()) {
            player.sendMessage(ChatUtils.colorize(message));
        }
    }

    public void sendStartMessage() {
        for (Player player : players.keySet()) {
            for (String line : Tnttag.configfile.getStringList("startMessage")) {
                player.sendMessage(ChatUtils.colorize(line));
            }
        }
    }
    public void teleportToLobby(Player player) {
        Location lobbyLocation = gameManager.arena.getStartLocation();
        player.teleport(lobbyLocation);
    }

    public void teleportToStart() {
        Location startLocation = gameManager.arena.getStartLocation();
        for (Player player : players.keySet()) {
            player.teleport(startLocation);
        }
    }

    private void setType(Player player, PlayerType type) {
        for (Map.Entry<Player, PlayerType> entry : players.entrySet()) {
            if (entry.getKey().getName().equals(player.getName())) {
                entry.setValue(type);
            }
        }
    }

    public void givePotionEffects(Player player) {
        player.getActivePotionEffects().clear();
        for (String potionEffect : gameManager.arena.getPotionEffects()) {
            String[] parts = potionEffect.split(":");
            if (parts[0] == null || parts[1] == null || parts[2] == null) {
                Bukkit.getLogger().severe("[TNT-Tag] Some of the potionEffects from arena " + gameManager.arena.getName() + " are misconfigured, the effects are not given.");
                break;
            }

            if (parts[2].equalsIgnoreCase("SURVIVORS")) {
                if (players.get(player).equals(PlayerType.SURVIVOR)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(parts[0]), 2147483647, Integer.parseInt(parts[1])));
                }
            }

            if (parts[2].equalsIgnoreCase("TAGGERS")) {
                if (players.get(player).equals(PlayerType.TAGGER)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(parts[0]), 2147483647, Integer.parseInt(parts[1])));
                }
            }
        }
    }

    public void pickPlayers() {
        Player tagger = null;
        List<Player> playersList = new ArrayList<>(players.keySet());

        // Keep selecting a random player until a non-spectator player is found
        while (tagger == null || players.get(tagger) == PlayerType.SPECTATOR) {
            int randomPlayerIndex = random.nextInt(players.size());
            tagger = playersList.get(randomPlayerIndex);
        }

        setType(tagger, PlayerType.TAGGER);
        ChatUtils.sendMessage(tagger, "player.is-tagger");
        gameManager.itemManager.giveTaggerItems(tagger);

        for (Player p : players.keySet()) {
            // Only process players that are not spectators
            if (players.get(p) != PlayerType.SPECTATOR) {
                // Set the player's type to PlayerType.SURVIVOR if they are not the tagger
                if (!p.equals(tagger)) {
                    setType(p, PlayerType.SURVIVOR);
                }
            }
            givePotionEffects(p);
        }
    }



    public int getPlayerCount() {
        //Do NOT count spectators!!
        int count = 0;
        for (Map.Entry<Player, PlayerType> entry : players.entrySet()) {
            if (entry.getValue() == PlayerType.SPECTATOR) continue;
            count++;
        }
        return count;
    }

    public HashMap<Player, PlayerType> getPlayers() {
        return players;
    }
}
