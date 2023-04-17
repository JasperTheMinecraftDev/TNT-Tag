package nl.juriantech.tnttag.handlers;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;

public class SetupCommandHandler implements Listener {

    private final Tnttag plugin = Tnttag.getInstance();
    private final ArenaManager arenaManager = plugin.getArenaManager();
    private String currentStep = "";
    private Player currentPlayer = null;
    private String arenaName = "";
    private int minPlayers = 0;
    private int maxPlayers = 0;
    private Location lobbyLocation = null;
    private Location startLocation = null;

    public SetupCommandHandler() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    public void start(Player player) {
        clearChat(player);
        ChatUtils.sendMessage(player, "setup.start");
        ChatUtils.sendMessage(player, "setup.enter-name");

        currentStep = "enter-name";
        currentPlayer = player;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (currentPlayer != null && currentPlayer.equals(player)) {
            event.setCancelled(true);

            if (message.equals("cancel")) {
                cancel(player);
                return;
            }

            if (currentStep.equals("enter-name")) {
                arenaName = message;
                clearChat(player);
                ChatUtils.sendMessage(player, "setup.name-entered");
                ChatUtils.sendMessage(player, "setup.enter-minPlayers");
                currentStep = "enter-minPlayers";
                return;
            }

            if (currentStep.equals("enter-minPlayers")) {
                int minPlayersInteger;
                try {
                    minPlayersInteger = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    ChatUtils.sendMessage(player, "general.invalid-number");
                    return;
                }
                if (minPlayersInteger < 2) {
                    ChatUtils.sendMessage(player, "setup.minPlayers-too-low");
                    return;
                }
                minPlayers = minPlayersInteger;
                clearChat(player);
                ChatUtils.sendMessage(player, "setup.minPlayers-entered");
                ChatUtils.sendMessage(player, "setup.enter-maxPlayers");
                currentStep = "enter-maxPlayers";
                return;
            }

            if (currentStep.equals("enter-maxPlayers")) {
                int maxPlayersInteger;
                try {
                    maxPlayersInteger = Integer.parseInt(message);
                } catch (NumberFormatException e) {
                    ChatUtils.sendMessage(player, "general.invalid-number");
                    return;
                }
                if (maxPlayersInteger <= minPlayers) {
                    ChatUtils.sendMessage(player, "general.maxPlayers-too-low");
                    return;
                }

                maxPlayers = maxPlayersInteger;
                clearChat(player);
                ChatUtils.sendMessage(player, "setup.maxPlayers-entered");
                ChatUtils.sendMessage(player, "setup.set-lobbyLocation");
                currentStep = "set-lobbyLocation";
                return;
            }

            if (currentStep.equals("set-lobbyLocation")) {
                if (message.equals("setlobby")) {
                    lobbyLocation = player.getLocation();
                    clearChat(player);
                    ChatUtils.sendMessage(player, "setup.lobbyLocation-set");
                    ChatUtils.sendMessage(player, "setup.set-startLocation");
                    currentStep = "set-startLocation";
                    return;
                }
            }

            if (currentStep.equals("set-startLocation")) {
                if (message.equals("setstart")) {
                    startLocation = player.getLocation();
                    clearChat(player);
                    ChatUtils.sendMessage(player, "setup.startLocation-set");
                    createArena(player);
                    // Unregister the event
                    HandlerList.unregisterAll(this);
                }
            }
        }
    }

    public void cancel(Player player) {
        ChatUtils.sendMessage(player, "setup.cancelled");
        clearChat(player);
        // Unregister the event
        HandlerList.unregisterAll(this);
    }

    public void clearChat(Player player) {
        int count = 0;

        while (count < 20) {
            count++;
            player.sendMessage("");
        }
    }

    public void createArena(Player player) {
        ArrayList<String> defaultPotionEffects = new ArrayList<>();
        defaultPotionEffects.add("SPEED:2:SURVIVORS");
        defaultPotionEffects.add("SPEED:3:TAGGERS");
        defaultPotionEffects.add("SLOW_FALLING:1:TAGGERS");
        defaultPotionEffects.add("SLOW_FALLING:1:SURVIVORS");
        defaultPotionEffects.add("HEALTH_BOOST:1:TAGGERS");
        defaultPotionEffects.add("HEALTH_BOOST:1:SURVIVORS");

        Arena arena = new Arena(plugin, arenaName, startLocation, lobbyLocation, maxPlayers, minPlayers, defaultPotionEffects, 60, 50);
        ChatUtils.sendMessage(arena, player, "setup.finished");

        arenaManager.saveArenaToFile(arena);
        arenaManager.arenaObjects.add(arena);
    }
}