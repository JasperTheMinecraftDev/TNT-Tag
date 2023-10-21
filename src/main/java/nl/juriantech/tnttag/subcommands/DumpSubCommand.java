package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.HashMap;
import java.util.Map;

@Command({"tnttag", "tt"})
public class DumpSubCommand {

    private final Tnttag plugin;
    private final Map<Player, Long> firstExecutionTimes = new HashMap<>();
    private final long executionTimeLimitMillis = 10000; // 10 seconds

    public DumpSubCommand(Tnttag plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                firstExecutionTimes.entrySet().removeIf(entry -> (currentTimeMillis - entry.getValue()) > executionTimeLimitMillis);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @Subcommand("dump all")
    @CommandPermission("tnttag.dump.all")
    public void onDumpAll(Player player) {
        if (hasExecutedTwice(player)) {
            plugin.getDumpManager().dumpAll(player);
            removeEntry(player);
        } else {
            addEntry(player);
            ChatUtils.sendMessage(player, "&cWarning: This command should only be used if requested by the developer.");
            ChatUtils.sendMessage(player, "&bExecute this command again within 10 seconds to proceed.");
        }
    }

    @Subcommand("dump log")
    @CommandPermission("tnttag.dump.log")
    public void onDumpLog(Player player) {
        if (hasExecutedTwice(player)) {
            plugin.getDumpManager().dumpLog(player);
            removeEntry(player);
        } else {
            addEntry(player);
            ChatUtils.sendMessage(player, "&cWarning: This command should only be used if requested by the developer.");
            ChatUtils.sendMessage(player, "&bExecute this command again within 10 seconds to proceed.");
        }
    }

    private boolean hasExecutedTwice(Player player) {
        long currentTimeMillis = System.currentTimeMillis();
        return firstExecutionTimes.containsKey(player) && (currentTimeMillis - firstExecutionTimes.get(player)) <= executionTimeLimitMillis;
    }

    private void addEntry(Player player) {
        firstExecutionTimes.put(player, System.currentTimeMillis());
    }

    private void removeEntry(Player player) {
        firstExecutionTimes.remove(player);
    }
}