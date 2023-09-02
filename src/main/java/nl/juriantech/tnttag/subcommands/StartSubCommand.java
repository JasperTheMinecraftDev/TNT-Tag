package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class StartSubCommand {

    private final ArenaManager arenaManager;

    public StartSubCommand(Tnttag plugin) {
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("start")
    @CommandPermission("tnttag.start")
    public void onStart(Player player, String arenaName, @Optional boolean forced) {
        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            ChatUtils.sendMessage(player, "commands.invalid-arena");
            return;
        }

        if (forced) {
            arena.getGameManager().setGameState(GameState.INGAME);
        } else {
            arena.getGameManager().start();
        }

        ChatUtils.sendMessage(player, "commands.arena-started");
    }
}