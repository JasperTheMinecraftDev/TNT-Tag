package nl.juriantech.tnttag.commands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.gui.ArenaEditorGUI;
import nl.juriantech.tnttag.gui.ArenaSelector;
import nl.juriantech.tnttag.gui.Stats;
import nl.juriantech.tnttag.gui.TopStats;
import nl.juriantech.tnttag.handlers.SetupCommandHandler;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Command({"tnttag", "tt"})
public class TnttagCommand {
    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public TnttagCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("help")
    @CommandPermission("tnttag.help")
    public void onHelp(Player player) {
        List<String> helpmenu = Tnttag.customizationfile.getStringList("help-menu");

        for (String message : helpmenu) {
            ChatUtils.sendCustomMessage(player, message);
        }
    }

    @Subcommand("join")
    @CommandPermission("tnttag.join")
    public void onJoin(Player player, @Optional String arenaName) {
        if (!plugin.getLobbyManager().playerIsInLobby(player)) {
            if (!plugin.getLobbyManager().enterLobby(player)) return;
        }

        if (arenaName != null) {
            if (arenaManager.playerIsInArena(player)) {
                ChatUtils.sendMessage(player, "player.already-in-game");
                return;
            }

            Arena arena = arenaManager.getArena(arenaName);
            if (arena == null) {
                ChatUtils.sendMessage(player, "commands.invalid-arena");
                return;
            }

            arena.getGameManager().playerManager.addPlayer(player);
        }
    }

    @Subcommand("joingui")
    @CommandPermission("tnttag.gui.join")
    public void onGUIJoin(Player player) {
        new ArenaSelector(plugin, player).open();
    }

    @Subcommand("leave")
    public void onLeave(Player player) {
        if (arenaManager.playerIsInArena(player)) {
            Arena arena = arenaManager.getPlayerArena(player);
            arena.getGameManager().playerManager.removePlayer(player, true);
        } else {
            ChatUtils.sendMessage(player, "commands.not-in-arena");
        }

        if (plugin.getLobbyManager().playerIsInLobby(player)) {
            plugin.getLobbyManager().leaveLobby(player);
        }
    }

    @Subcommand("list")
    @CommandPermission("tnttag.list")
    public void onList(Player player) {
        List<Arena> arenas = arenaManager.getArenaObjects();
        if (arenas.toArray().length == 0) {
            ChatUtils.sendMessage(player, "commands.no-available-arenas");
            return;
        }

        String arenaNames = arenas.stream()
                .map(Arena::getName)
                .collect(Collectors.joining());
        ChatUtils.sendCustomMessage(player, ChatUtils.getRaw("commands.available-arenas").replace("{arenas}", arenaNames));
    }

    @Subcommand("create")
    @CommandPermission("tnttag.create")
    public void onCreate(Player player) {
        new SetupCommandHandler(plugin).start(player);
    }

    @Subcommand("editor")
    @CommandPermission("tnttag.editor")
    public void onEditor(Player player, String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            if (arena.getGameManager().isRunning()) {
                ChatUtils.sendMessage(player, "commands.arena-is-running");
                return;
            }
            new ArenaEditorGUI(plugin, player, arena).open();
        } else {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
        }
    }

    @Subcommand("delete")
    @CommandPermission("tnttag.delete")
    public void onDelete(Player player, String arenaName) throws IOException {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena != null) {
            arenaManager.deleteArena(arenaName);
            ChatUtils.sendMessage(arena, player, "commands.arena-deleted");
        } else {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
        }
    }

    @Subcommand("reload")
    @CommandPermission("tnttag.reload")
    public void onReload(Player player) throws IOException {
        Tnttag.customizationfile.reload();
        Tnttag.configfile.reload();
        arenaManager.reload();

        ChatUtils.sendMessage(player, "commands.files-reloaded");
    }

    @Subcommand("stats")
    @CommandPermission("tnttag.stats")
    public void onStats(Player player) {
        new Stats(plugin, player).open();
    }

    @Subcommand("top")
    @CommandPermission("tnttag.top")
    public void onTop(Player player, String type) {
        new TopStats(plugin, player, type).open();
    }

    @Subcommand("start")
    @CommandPermission("tnttag.start")
    public void onStart(Player player, String arenaName) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
            return;
        }

        arena.getGameManager().start();
        ChatUtils.sendMessage(player, "commands.arena-started");
    }

    @Subcommand("info")
    @CommandPermission("tnttag.info")
    public void onInfo(Player player, String arena) {
        Arena arenaObj = arenaManager.getArena(arena);
        if (arenaObj == null) {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
            return;
        }

        int countdown = arenaObj.getCountdown();
        int minPlayers = arenaObj.getMinPlayers();
        int maxPlayers = arenaObj.getMaxPlayers();
        ChatUtils.sendCustomMessage(player, "&6Countdown: " + countdown + ", minPlayers: " + minPlayers + ", maxPlayers: " + maxPlayers);
    }

    //setlobby command for the global lobby
    @Subcommand("setlobby")
    @CommandPermission("tnttag.setlobby")
    public void onSetLobby(Player player) throws IOException {
        Location loc = player.getLocation();
        
        Tnttag.configfile.set(
                "globalLobby",
                loc.getWorld().getName() + ","
                + loc.getX() + ","
                + loc.getY() + ","
                + loc.getZ() + ","
                + loc.getYaw() + ","
                + loc.getPitch()
        );

        Tnttag.configfile.save();

        ChatUtils.sendMessage(player, "commands.global-lobby-set");
    }
}
