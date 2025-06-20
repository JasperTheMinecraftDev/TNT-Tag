package nl.juriantech.tnttag.objects;

import nl.juriantech.tnttag.Tnttag;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PlayerInformation {

    private final Player player;
    private final Location oldLocation;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final int level; // Store player's level
    private final int totalExperience; // Store total experience points
    private final GameMode gameMode;
    private final int foodLevel;
    private final String displayName;
    private final String playerListName;
    private String tabPrefix;

    public PlayerInformation(Tnttag plugin, Player player) {
        this.player = player;
        this.oldLocation = player.getLocation();
        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.level = player.getLevel();
        this.totalExperience = player.getTotalExperience();
        this.gameMode = player.getGameMode();
        this.foodLevel = player.getFoodLevel();
        this.displayName = player.getDisplayName();
        this.playerListName = player.getPlayerListName();

        if (plugin.getTabHook() != null) {
            tabPrefix = plugin.getTabHook().getPlayerPrefix(player.getUniqueId());
        }

        player.getInventory().clear();
        player.setExp(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
    }

    public void restore() {
        List<PotionEffect> activeEffects = new ArrayList<>(player.getActivePotionEffects());
        activeEffects.forEach(activePotionEffect -> player.removePotionEffect(activePotionEffect.getType()));

        player.getInventory().clear();
        player.getInventory().setContents(inventory);
        player.getInventory().setArmorContents(armor);
        player.setTotalExperience(totalExperience);
        player.setLevel(level);

        if (!Tnttag.configfile.getBoolean("skip-location-restoral")) player.teleport(oldLocation);

        player.setGameMode(gameMode);
        player.setFoodLevel(foodLevel);
        player.setDisplayName(displayName);
        player.setPlayerListName(playerListName);
    }

    public Player getPlayer() {
        return player;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPlayerListName() {
        return playerListName;
    }

    public String getTabPrefix() {
        return tabPrefix;
    }
}