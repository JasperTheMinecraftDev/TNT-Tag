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
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TopSign implements SignInterface {

    private final Location loc;
    private final int position;
    private final StatType statType;
    private final List<String> signLines;
    private final String formattedStatType;

    public TopSign(Location loc, int position, StatType statType) {
        this.loc = loc;
        this.position = position;
        this.statType = statType;

        this.signLines = Tnttag.customizationfile.getStringList("lines");
        this.formattedStatType = Tnttag.customizationfile.getString("types." + statType.toString());
    }

    @Override
    public void onClick(Player player) {
        //Nothing here!
    }

    @Override
    public void update() {
        if (loc == null) return;

        Block block = loc.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        API api = Tnttag.getAPI();
        TreeMap<UUID, Integer> data;

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

        String playerName = Tnttag.customizationfile.getString("top-sign.no-data");

        if (!topTenPlayers.isEmpty()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(topTenPlayers.get(position - 1).getKey()); //array is zero based

            if (player.getName() != null) {
                playerName = player.getName();
            }
        }

        for (int i = 0; i <= 3; i++) {
            String line = signLines.get(i);

            line = line.replace("{top_type}", formattedStatType)
                    .replace("{top_position}", String.valueOf(position))
                    .replace("{player}", playerName);

            sign.setLine(i, ChatUtils.colorize(line));
        }

        sign.update(true);
    }

    @Override
    public String toString() {
        return SimpleLocation.fromLocation(loc) + ";" + position + ";" + statType.toString();
    }

    public static TopSign fromString(String str) {
        String[] parts = str.split(";");

        if (parts.length == 3) {
            Location location = Objects.requireNonNull(SimpleLocation.fromString(parts[0])).toLocation();
            int position = Integer.parseInt(parts[1]);
            StatType statType = StatType.valueOf(parts[2]);

            return new TopSign(location, position, statType);
        }

        return null;
    }

    @Override
    public Location getLoc() {
        return loc;
    }
}
