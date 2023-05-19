package nl.juriantech.tnttag.signs;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.objects.SimpleLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LeaveSign implements SignInterface {

    private final Tnttag plugin;
    private final Location loc;

    public LeaveSign(Tnttag plugin, Location loc) {
        this.plugin = plugin;
        this.loc = loc;
    }

    @Override
    public void onClick(Player player) {
        player.performCommand("tnttag leave");
    }

    @Override
    public void update() {
        if (loc != null) {
            Block block = loc.getBlock();
            if (block.getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();

                sign.setLine(0, SignManager.SIGN_PREFIX);
                sign.setLine(1, ChatColor.WHITE + "leave");
                sign.setLine(2, "");
                sign.setLine(3, "");
                sign.update(true);
            }
        }
    }

    @Override
    public String toString() {
        return SimpleLocation.fromLocation(loc).toString();
    }

    public static LeaveSign fromString(Tnttag plugin, String str) {
        Location location = SimpleLocation.fromString(str).toLocation();
        return new LeaveSign(plugin, location);
    }

    @Override
    public Location getLoc() {
        return loc;
    }
}