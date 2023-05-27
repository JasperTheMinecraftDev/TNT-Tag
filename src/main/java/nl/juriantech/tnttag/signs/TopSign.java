package nl.juriantech.tnttag.signs;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.api.API;
import nl.juriantech.tnttag.enums.StatType;
import nl.juriantech.tnttag.objects.SimpleLocation;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TopSign implements SignInterface {

    private final Tnttag plugin;
    private final Location loc;
    private final int position;
    private final StatType statType;

    public TopSign(Tnttag plugin, Location loc, int position, StatType statType) {
        this.plugin = plugin;
        this.loc = loc;
        this.position = position;
        this.statType = statType;
    }

    @Override
    public void onClick(Player player) {
        //Nothing here!
    }

    @Override
    public void update() {
        if (loc == null) return;

        Block block = loc.getBlock();
        if (!(block.getState() instanceof org.bukkit.block.Sign)) return;

        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
        API api = Tnttag.getAPI();
        TreeMap<UUID, Integer> data = null;

        switch (statType) {
            case WINS:
                data = api.getWinsData();
                break;
            case TIMESTAGGED:
                data = api.getTimesTaggedData();
                break;
            case TAGS:
                data = api.getTagsData();
                break;
            default:
                return;
        }


        List<Map.Entry<UUID, Integer>> topTenPlayers = data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());

        String playerName = "NOBODY";

        OfflinePlayer player = Bukkit.getOfflinePlayer(topTenPlayers.get(position - 1).getKey()); //array is zero based

        if (player.getName() != null) {
            playerName = player.getName();
        }

        for (int i = 0; i <= 3; i++) {
            sign.setLine(i, ChatUtils.colorize(
                    Tnttag.customizationfile.getStringList("top-sign.lines").get(i)
                            .replace("%top_type%", Tnttag.customizationfile.getString("top-sign.types." + statType.toString()))
                            .replace("%top_position%", String.valueOf(position))
                            .replace("%player%", playerName)
                    )
            );
        }

        sign.update(true);
    }

    @Override
    public String toString() {
        return SimpleLocation.fromLocation(loc).toString() + ";" + position + ";" + statType.toString();
    }

    public static TopSign fromString(Tnttag plugin, String str) {
        String[] parts = str.split(";");

        if (parts.length == 3) {
            Location location = SimpleLocation.fromString(parts[0]).toLocation();
            int position = Integer.parseInt(parts[1]);
            StatType statType = StatType.valueOf(parts[2]);

            return new TopSign(plugin, location, position, statType);
        }

        return null;
    }

    @Override
    public Location getLoc() {
        return loc;
    }
}
