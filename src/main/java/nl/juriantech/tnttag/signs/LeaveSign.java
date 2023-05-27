package nl.juriantech.tnttag.signs;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.SimpleLocation;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LeaveSign implements SignInterface {

    private final Location loc;

    public LeaveSign(Tnttag plugin, Location loc) {
        this.loc = loc;
    }

    @Override
    public void onClick(Player player) {
        player.performCommand("tnttag leave");
    }

    @Override
    public void update() {
        if (loc == null) return;

        Block block = loc.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Sign)) return;

        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();

        for (int i = 0; i <= 3; i++) {
            sign.setLine(i, ChatUtils.colorize(Tnttag.customizationfile.getStringList("leave-sign.lines").get(i)));
        }

        sign.update(true);
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