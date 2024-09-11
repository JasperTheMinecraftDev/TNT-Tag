package nl.juriantech.tnttag.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.objects.PlayerData;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
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
        if (player.getPlayer() == null) {
            return "Error occurred: player is null.";
        }

        PlayerData playerData = new PlayerData(player.getPlayer().getUniqueId());

        if (params.equalsIgnoreCase("wins")) {
            return String.valueOf(playerData.getWins());
        }

        if (params.equalsIgnoreCase("timestagged")) {
            return String.valueOf(playerData.getTimesTagged());
        }

        if (params.equalsIgnoreCase("tags")) {
            return String.valueOf(playerData.getTags());
        }

        if (params.equalsIgnoreCase("winstreak")) {
            return String.valueOf(playerData.getWinstreak());
        }

        if (params.startsWith("top_wins_") || params.startsWith("top_timestagged_") || params.startsWith("top_tags_")) {
            String[] parts = params.split("_");
            if (parts.length == 3) {
                int position = Integer.parseInt(parts[2]);
                TreeMap<UUID, Integer> data = null;

                if (params.startsWith("top_wins_")) {
                    data = Tnttag.getAPI().getWinsData();
                } else if (params.startsWith("top_timestagged_")) {
                    data = Tnttag.getAPI().getTimesTaggedData();
                } else if (params.startsWith("top_tags_")) {
                    data = Tnttag.getAPI().getTagsData();
                } else if (params.startsWith("top_winstreak_")) {
                    data = Tnttag.getAPI().getWinstreakData();
                }

                if (data != null) {
                    List<Map.Entry<UUID, Integer>> sortedEntries = data.entrySet().stream()
                            .sorted((entry1, entry2) -> Integer.compare(entry2.getValue(), entry1.getValue()))
                            .collect(Collectors.toList());

                    if (position >= 1 && position <= sortedEntries.size()) {
                        Map.Entry<UUID, Integer> entry = sortedEntries.get(position - 1);
                        String placeholderType = params.startsWith("top_wins_") ? "wins" :
                                params.startsWith("top_timestagged_") ? "timestagged" :
                                        params.startsWith("top_winstreak_") ? "winstreak" : "tags";

                        return ChatUtils.colorize(Tnttag.customizationfile.getString("top-placeholder-formatting." + placeholderType)
                                .replace("%player%", Objects.requireNonNull(Bukkit.getOfflinePlayer(entry.getKey()).getName()))
                                .replace("%amount%", String.valueOf(entry.getValue())));
                    }

                    return "N/A";
                }
            }
        }

        if (params.startsWith("arena_")) {
            String[] parts = params.split("_");
            if (parts.length == 3) {
                String arenaName = parts[1];
                String type = parts[2];

                Arena arena = null;
                if (arenaName.equals("current")) {
                    if (plugin.getArenaManager().playerIsInArena(player.getPlayer())) {
                        arena = plugin.getArenaManager().getPlayerArena(Bukkit.getPlayer(player.getUniqueId()));
                    } else {
                        return "Player not in arena.";
                    }
                } else {
                    arena = plugin.getArenaManager().getArena(arenaName);
                    if (arena == null) {
                        return "Invalid arena";
                    }
                }

                switch (type) {
                    case "currentPlayers":
                        return String.valueOf(arena.getGameManager().playerManager.getPlayerCount());
                    case "minPlayers":
                        return String.valueOf(arena.getMinPlayers());
                    case "maxPlayers":
                        return String.valueOf(arena.getMaxPlayers());
                    case "survivors":
                    case "taggers":
                    case "spectators":
                        if (!arena.getGameManager().isRunning()) {
                            return String.valueOf(0);
                        }
                        int count = 0;
                        for (Map.Entry<Player, PlayerType> entry : arena.getGameManager().playerManager.getPlayers().entrySet()) {
                            if ((type.equals("survivors") && entry.getValue().equals(PlayerType.SURVIVOR))  ||
                                    (type.equals("taggers") && entry.getValue().equals(PlayerType.TAGGER)) ||
                                    (type.equals("spectators") && entry.getValue().equals(PlayerType.SPECTATOR))) {
                                count++;
                            }
                        }
                        return String.valueOf(count);
                    case "state":
                        return String.valueOf(arena.getGameManager().state);
                    default:
                        return "Invalid type";
                }
            }
        }

        return null; // Placeholder is unknown by the Expansion
    }

    public String parse(Player player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
