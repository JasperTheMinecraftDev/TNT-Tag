package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class JoinSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public JoinSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("join")
    @CommandPermission("tnttag.join")
    public void onJoin(Player player, @Optional String arenaName) {
        if (arenaName == null && !Tnttag.configfile.getBoolean("global-lobby")) {
            player.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("general.specify-arena")));
            return;
        }

        if (!plugin.getLobbyManager().playerIsInLobby(player)) {
            if (!plugin.getLobbyManager().enterLobby(player)) return;
        } else if (arenaName == null) {
            ChatUtils.sendMessage(player, "player.already-in-lobby");
            return;
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
}