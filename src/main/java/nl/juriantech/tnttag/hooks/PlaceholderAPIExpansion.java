package nl.juriantech.tnttag.hooks;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerData;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

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
