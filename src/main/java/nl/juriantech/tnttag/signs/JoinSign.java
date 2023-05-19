package nl.juriantech.tnttag.signs;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.objects.SimpleLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class JoinSign implements SignInterface {

    private final Tnttag plugin;
    private final String arena;
    private final Location loc;

    public JoinSign(Tnttag plugin, String arena, Location loc) {
        this.plugin = plugin;
        this.arena = arena;
        this.loc = loc;
    }

    @Override
    public void onClick(Player player) {
        player.performCommand("tnttag join " + arena);
    }

    @Override
    public void update() {
        if (loc != null) {
            Block block = loc.getBlock();
            if (block.getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
                Arena arena = plugin.getArenaManager().getArena(this.arena);
                if (arena == null) {
                    return;
                }
                int currentPlayers = arena.getGameManager().playerManager.getPlayerCount();
                int maxPlayers = arena.getMaxPlayers();
                sign.setLine(0, SignManager.SIGN_PREFIX);
                sign.setLine(1, ChatColor.WHITE + this.arena);
                sign.setLine(2, arena.getGameManager().getCustomizedState() + ChatColor.RESET + ": " + ChatColor.GOLD + ChatColor.BOLD + currentPlayers
                        + ChatColor.YELLOW + "/" + ChatColor.WHITE + ChatColor.BOLD + maxPlayers);
                sign.update(true);
            }
        }
    }

    @Override
    public String toString() {
        return arena + ";" + SimpleLocation.fromLocation(loc).toString();
    }

    public static JoinSign fromString(Tnttag plugin, String str) {
        String[] parts = str.split(";");

        if (parts.length == 2) {
            String arena = parts[0];
            Location location = SimpleLocation.fromString(parts[1]).toLocation();
            return new JoinSign(plugin, arena, location);
        }

        return null;
    }

    @Override
    public Location getLoc() {
        return loc;
    }
}
