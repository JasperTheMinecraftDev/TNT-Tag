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
public class InfoSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public InfoSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("info")
    @CommandPermission("tnttag.info")
    public void onInfo(Player player, String arena) {
        Arena arenaObj = arenaManager.getArena(arena);
        if (arenaObj == null) {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
            return;
        }

        for (String string : Tnttag.customizationfile.getStringList("info-command")) {
            ChatUtils.sendCustomMessage(player,
                    string
                            .replace("{arena_name}", arenaObj.getName())
                            .replace("{is_running}", String.valueOf(arenaObj.getGameManager().isRunning()))
                            .replace("{countdown}", String.valueOf(arenaObj.getCountdown()))
                            .replace("{min_players}", String.valueOf(arenaObj.getMinPlayers()))
                            .replace("{max_players}", String.valueOf(arenaObj.getMaxPlayers()))
            );
        }
    }
}