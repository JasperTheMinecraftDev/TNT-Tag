package nl.juriantech.tnttag.hooks;

import me.neznamy.tab.api.TabAPI;
import nl.juriantech.tnttag.Tnttag;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class TabHook {

    private final Tnttag plugin;
    private final TabAPI tabAPI;

    public TabHook(Tnttag plugin) {
        this.plugin = plugin;
        this.tabAPI = TabAPI.getInstance();
    }

    public void hidePlayerName(UUID playerUUID) {
        if (tabAPI.getNameTagManager() != null) {
            tabAPI.getNameTagManager().hideNameTag(Objects.requireNonNull(tabAPI.getPlayer(playerUUID)));
        }
    }

    public void showPlayerName(UUID playerUUID) {
        if (tabAPI.getNameTagManager() != null) {
            tabAPI.getNameTagManager().showNameTag(Objects.requireNonNull(tabAPI.getPlayer(playerUUID)));
        }
    }

    public void setPlayerPrefix(UUID playerUUID, String prefix) {
        if (plugin.getPlaceholderAPIExpansion() != null) plugin.getPlaceholderAPIExpansion().parse(Bukkit.getPlayer(playerUUID), prefix);

        if (tabAPI.getNameTagManager() != null) {
            tabAPI.getNameTagManager().setPrefix(Objects.requireNonNull(tabAPI.getPlayer(playerUUID)), prefix);
        }

        if (tabAPI.getTabListFormatManager() != null) {
            tabAPI.getTabListFormatManager().setPrefix(Objects.requireNonNull(tabAPI.getPlayer(playerUUID)), prefix);
        }
    }

    public String getPlayerPrefix(UUID playerUUID) {
        if (tabAPI.getNameTagManager() == null) return null;

        return tabAPI.getNameTagManager().getCustomPrefix(Objects.requireNonNull(tabAPI.getPlayer(playerUUID)));
    }
}