package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;

@Command({"tnttag", "tt"})
public class LeaveSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public LeaveSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
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
            if (Tnttag.configfile.getBoolean("bungee-mode.enabled")) {
                //The leaveLobby(player) method will automatically be executed once they actually leave, otherwise they can keep being in the server
                //if the lobby is offline.
                plugin.connectToServer(player, Tnttag.configfile.getString("bungee-mode.lobby-server"));
            } else {
                plugin.getLobbyManager().leaveLobby(player);
            }
        }
    }
}