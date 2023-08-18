package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.gui.Stats;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class StatsSubCommand {

    private final Tnttag plugin;

    public StatsSubCommand(Tnttag plugin) {
        this.plugin = plugin;
    }

    @Subcommand("stats")
    @CommandPermission("tnttag.stats")
    public void onStats(Player player) {
        new Stats(plugin, player).open();
    }
}