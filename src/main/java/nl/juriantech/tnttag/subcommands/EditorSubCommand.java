package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.gui.ArenaEditorGUI;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class EditorSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public EditorSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
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
}