package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Tnttag;
import org.bukkit.command.CommandSender;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class DumpSubCommand {

    private final Tnttag plugin;

    public DumpSubCommand(Tnttag plugin) {
        this.plugin = plugin;
    }

    @Subcommand("dump all")
    @CommandPermission("tnttag.dump.all")
    public void onDumpAll(CommandSender commandSender) {
        plugin.getDumpManager().dumpAll(commandSender);
    }

    @Subcommand("dump log")
    @CommandPermission("tnttag.dump.log")
    public void onDumpLog(CommandSender commandSender) {
        plugin.getDumpManager().dumpLog(commandSender);
    }
}