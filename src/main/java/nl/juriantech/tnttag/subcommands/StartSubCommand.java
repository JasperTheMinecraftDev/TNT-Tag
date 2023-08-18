package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.IOException;

@Command({"tnttag", "tt"})
public class StartSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public StartSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
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
}