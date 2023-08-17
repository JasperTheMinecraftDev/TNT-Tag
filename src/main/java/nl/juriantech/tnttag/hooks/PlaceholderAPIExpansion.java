package nl.juriantech.tnttag.hooks;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerData;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final Tnttag plugin;

    public PlaceholderAPIExpansion(Tnttag plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "tnttag";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player.getPlayer() == null) return "Error occurred: player is null.";

        PlayerData playerData = new PlayerData(player.getPlayer().getUniqueId());
        if(params.equalsIgnoreCase("wins")){
            return String.valueOf(playerData.getWins());
        }

        if(params.equalsIgnoreCase("timestagged")){
            return String.valueOf(playerData.getTimesTagged());
        }

        if(params.equalsIgnoreCase("tags")){
            return String.valueOf(playerData.getTags());
        }

        if (params.startsWith("top_wins_")) {
            int position = Integer.parseInt(params.substring("top_wins_".length()));
            TreeMap<UUID, Integer> winsData = Tnttag.getAPI().getWinsData();

            List<Map.Entry<UUID, Integer>> sortedEntries = winsData.entrySet().stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue())) // Sort in descending order
                    .collect(Collectors.toList());

            if (position >= 1 && position <= sortedEntries.size()) {
                Map.Entry<UUID, Integer> entry = sortedEntries.get(position - 1);
                return ChatUtils.colorize(Tnttag.customizationfile.getString("top-placeholder-formatting.wins")
                        .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(entry.getKey()).getName()))
                        .replace("%amount%", String.valueOf(entry.getValue())));
            }

            return "N/A"; // No data available or invalid position
        }

        if (params.startsWith("top_timestagged_")) {
            int position = Integer.parseInt(params.substring("top_timestagged_".length()));
            TreeMap<UUID, Integer> timesTaggedData = Tnttag.getAPI().getTimesTaggedData();

            List<Map.Entry<UUID, Integer>> sortedEntries = timesTaggedData.entrySet().stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue())) // Sort in descending order
                    .collect(Collectors.toList());

            if (position >= 1 && position <= sortedEntries.size()) {
                Map.Entry<UUID, Integer> entry = sortedEntries.get(position - 1);
                return ChatUtils.colorize(Tnttag.customizationfile.getString("top-placeholder-formatting.timestagged")
                        .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(entry.getKey()).getName()))
                        .replace("%amount%", String.valueOf(entry.getValue())));
            }

            return "N/A"; // No data available or invalid position
        }

        if (params.startsWith("top_tags_")) {
            int position = Integer.parseInt(params.substring("top_tags_".length()));
            TreeMap<UUID, Integer> tagsData = Tnttag.getAPI().getTagsData();

            List<Map.Entry<UUID, Integer>> sortedEntries = tagsData.entrySet().stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue())) // Sort in descending order
                    .collect(Collectors.toList());

            if (position >= 1 && position <= sortedEntries.size()) {
                Map.Entry<UUID, Integer> entry = sortedEntries.get(position - 1);
                return ChatUtils.colorize(Tnttag.customizationfile.getString("top-placeholder-formatting.tags")
                        .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(entry.getKey()).getName()))
                        .replace("%amount%", String.valueOf(entry.getValue())));
            }

            return "N/A"; // No data available or invalid position
        }

        if(params.startsWith("arena_")){
            // Split the placeholder into its components
            String[] parts = params.split("_");
            if (parts.length == 2) {
                String arenaName = parts[0];
                String type = parts[1];

                Arena arena = plugin.getArenaManager().getArena(arenaName);
                if (arena == null) return "Invalid arena";

                if (type.equals("currentPlayers")) {
                    return String.valueOf(arena.getGameManager().playerManager.getPlayerCount());
                }

                if (type.equals("minPlayers")) {
                    return String.valueOf(arena.getMinPlayers());
                }

                if (type.equals("maxPlayers")) {
                    return String.valueOf(arena.getMaxPlayers());
                }

                if (type.equals("state")) {
                    return String.valueOf(arena.getGameManager().state);
                }
                return "Invalid type";
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }
}
