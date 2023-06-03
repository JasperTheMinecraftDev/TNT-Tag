package nl.juriantech.tnttag.managers;

import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.api.PlayerJoinArenaEvent;
import nl.juriantech.tnttag.api.PlayerLeaveArenaEvent;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.hooks.PartyAndFriendsHook;
import nl.juriantech.tnttag.objects.PlayerData;
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
    private final HashMap<Player, PlayerType> players;
    private final LobbyManager lobbyManager;

    public PlayerManager(Tnttag plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.players = new HashMap<>();
        this.lobbyManager = plugin.getLobbyManager();
    }

    public synchronized void addPlayer(Player player) {
        if (!lobbyManager.playerIsInLobby(player)) {
            lobbyManager.enterLobby(player);
        }

        int minPlayers = gameManager.arena.getMinPlayers();
        int maxPlayers = gameManager.arena.getMaxPlayers();

        if (gameManager.state == GameState.INGAME || getPlayerCount() == maxPlayers) return; //Safety check.
        if (players.containsKey(player)) return; //Safety check.

        if (plugin.getPartyAndFriendsHook() != null) {
            PartyAndFriendsHook hook = plugin.getPartyAndFriendsHook();
            if (hook.playerIsInParty(player.getUniqueId())) {
                PlayerParty party = hook.getPlayerParty(player.getUniqueId());
                if (party.getLeader().getUniqueId().equals(player.getUniqueId())) {
                    int partySize = hook.getPlayersOfParty(party).size();
                    if (players.size() + partySize > maxPlayers) {
                        ChatUtils.sendMessage(player, "party.too-much-players");
                        return;
                    }

                    for (Player partyPlayer : hook.getPlayersOfParty(party)) {
                        addPlayer(partyPlayer);
                        ChatUtils.sendMessage(player, "party.joined-game");
                    }
                } else {
                    ChatUtils.sendMessage(player, "party.not-the-leader");
                }
            }
        }

        PlayerJoinArenaEvent event = new PlayerJoinArenaEvent(player, gameManager.arena.getName());
        Bukkit.getPluginManager().callEvent(event);

        players.put(player, PlayerType.WAITING);
        gameManager.itemManager.giveLobbyItems(player);
        teleportToLobby(player);
        ChatUtils.sendMessage(player, "player.joined-arena");
        broadcast(ChatUtils.getRaw("arena.player-joined").replace("{player}", player.getName()));

        if (getPlayerCount() >= minPlayers) {
            gameManager.start();
        }
    }

    public synchronized void removePlayer(Player player, boolean message) {
        if (!players.containsKey(player)) return;
        PlayerLeaveArenaEvent event = new PlayerLeaveArenaEvent(player, gameManager.arena.getName());
        Bukkit.getPluginManager().callEvent(event);

        setPlayerType(player, PlayerType.WAITING);
        players.remove(player);
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

        if (players.size() == 0) gameManager.stop();
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
            player.setInvisible(false);
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
                if (players.get(player) != PlayerType.TAGGER) {
                    player.teleport(gameManager.arena.getStartLocation());
                }

                setType(player, PlayerType.SURVIVOR);
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
                player.setInvisible(true);
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
        Location lobbyLocation = gameManager.arena.getLobbyLocation();
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
                    player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parts[0])), 2147483647, Integer.parseInt(parts[1])));
                }
            }

            if (parts[2].equalsIgnoreCase("TAGGERS")) {
                if (players.get(player).equals(PlayerType.TAGGER)) {
                    player.addPotionEffect(new PotionEffect(Objects.requireNonNull(PotionEffectType.getByName(parts[0])), 2147483647, Integer.parseInt(parts[1])));
                }
            }
        }
    }

    public void pickPlayers() {
        List<Player> playersList = new ArrayList<>(players.keySet());
        List<Player> taggers = new ArrayList<>();

        // Remove the percentage sign and convert to a double
        double taggerPercentage = Double.parseDouble(Tnttag.configfile.getString("taggers-percentage").replace("%", "")) / 100.0;
        int numTaggers = (int) Math.round(playersList.size() * taggerPercentage);

        // Shuffle the players list to introduce randomness
        Collections.shuffle(playersList);

        for (int i = 0; i < numTaggers; i++) {
            Player player = playersList.get(i);
            taggers.add(player);
            setType(player, PlayerType.TAGGER);
            ChatUtils.sendMessage(player, "player.is-tagger");
            gameManager.itemManager.giveTaggerItems(player);
        }

        for (Player p : players.keySet()) {
            // Only process players that are not spectators
            if (players.get(p) != PlayerType.SPECTATOR) {
                // Set the player's type to PlayerType.SURVIVOR if they are not a tagger
                if (!taggers.contains(p)) {
                    setType(p, PlayerType.SURVIVOR);
                }
                givePotionEffects(p);
            }
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
