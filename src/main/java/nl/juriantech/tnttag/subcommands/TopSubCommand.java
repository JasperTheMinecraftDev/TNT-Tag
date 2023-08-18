package nl.juriantech.tnttag.subcommands;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.gui.Stats;
import nl.juriantech.tnttag.gui.TopStats;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command({"tnttag", "tt"})
public class TopSubCommand {

    private final Tnttag plugin;

    public TopSubCommand(Tnttag plugin) {
        this.plugin = plugin;
    }

    @Subcommand("top")
    @CommandPermission("tnttag.top")
    public void onTop(Player player, String type) {
        new TopStats(plugin, player, type).open();
    }
}