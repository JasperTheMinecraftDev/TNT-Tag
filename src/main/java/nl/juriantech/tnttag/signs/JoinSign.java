package nl.juriantech.tnttag.signs;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.SimpleLocation;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class JoinSign implements SignInterface {

    private final Tnttag plugin;
    private final String arena;
    private final Location loc;
    private final List<String> signLines;

    public JoinSign(Tnttag plugin, String arena, Location loc) {
        this.plugin = plugin;
        this.arena = arena;
        this.loc = loc;

        this.signLines = Tnttag.customizationfile.getStringList("join-sign.lines");
    }

    @Override
    public void onClick(Player player) {
        player.getInventory().setHeldItemSlot(0);
        player.performCommand("tnttag join " + arena);
    }

    @Override
    public void update() {
        if (loc == null) return;

        Block block = loc.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Sign)) return;

        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
        Arena arena = plugin.getArenaManager().getArena(this.arena);
        if (arena == null) {
            return;
        }

        int currentPlayers = arena.getGameManager().playerManager.getPlayerCount();
        int maxPlayers = arena.getMaxPlayers();

        for (int i = 0; i <= 3; i++) {
            sign.setLine(i, ChatUtils.colorize(
                    signLines.get(i)
                            .replace("{arena}", this.arena)
                            .replace("{state}", arena.getGameManager().getCustomizedState())
                            .replace("{current_players}", String.valueOf(currentPlayers))
                            .replace("{max_players}", String.valueOf(maxPlayers))
                    )
            );
        }

        sign.update(true);
    }

    @Override
    public String toString() {
        return arena + ";" + SimpleLocation.fromLocation(loc);
    }

    public static JoinSign fromString(Tnttag plugin, String str) {
        String[] parts = str.split(";");

        if (parts.length == 2) {
            String arena = parts[0];
            Location location = Objects.requireNonNull(SimpleLocation.fromString(parts[1])).toLocation();
            return new JoinSign(plugin, arena, location);
        }

        return null;
    }

    @Override
    public Location getLoc() {
        return loc;
    }
}
