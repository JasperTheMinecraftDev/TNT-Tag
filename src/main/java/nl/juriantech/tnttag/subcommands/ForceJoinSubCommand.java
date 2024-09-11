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
public class ForceJoinSubCommand {

    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public ForceJoinSubCommand(Tnttag plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @Subcommand("forcejoin")
    @CommandPermission("tnttag.forcejoin")
    public void onJoin(Player executor, @Optional String arenaName) {
        if (arenaName == null && !Tnttag.configfile.getBoolean("global-lobby")) {
            executor.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("general.specify-arena")));
            return;
        }

        Arena arena = null;
        if (arenaName != null) {
            arena = arenaManager.getArena(arenaName);
            if (arena == null) {
                executor.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("commands.invalid-arena")));
                return;
            }

            if (arena.getGameManager().isRunning()) {
                executor.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("arena.active")));
                return;
            }

            if (arena.getGameManager().playerManager.getPlayers().size() + plugin.getServer().getOnlinePlayers().size() >= arena.getMaxPlayers()) {
                executor.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("arena.full")));
                return;
            }
        }


        // Force join all players online
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            if (arenaName == null) {
                if (!plugin.getLobbyManager().playerIsInLobby(target)) {
                    plugin.getLobbyManager().enterLobby(target, true);
                }
            } else {
                if (!arenaManager.playerIsInArena(target) && !target.hasPermission("tnttag.bypass-forcejoin")) {
                    arena.getGameManager().playerManager.addPlayer(target);
                }
            }
        }

        executor.sendMessage(ChatUtils.colorize(Tnttag.customizationfile.getString("commands.forcejoin.success")));
    }
}