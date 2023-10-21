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
public class DeleteSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public DeleteSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("delete")
    @CommandPermission("tnttag.delete")
    public void onDelete(Player player, Arena arena) throws IOException {
        arenaManager.deleteArena(arena.getName());
        ChatUtils.sendMessage(arena, player, "commands.arena-deleted");
    }
}