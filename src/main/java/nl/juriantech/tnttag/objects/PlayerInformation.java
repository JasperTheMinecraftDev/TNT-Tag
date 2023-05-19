package nl.juriantech.tnttag.objects;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class PlayerInformation {

    private final Player player;
    private final Location oldLocation;
    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final float exp;
    private final GameMode gameMode;
    private final int foodLevel;

    public PlayerInformation(Player player) {
        this.player = player;
        this.oldLocation = player.getLocation();
        this.inventory = player.getInventory().getContents();
        this.armor = player.getInventory().getArmorContents();
        this.exp = player.getExp();
        this.gameMode = player.getGameMode();
        this.foodLevel = player.getFoodLevel();

        player.getInventory().clear();
        player.setExp(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
    }

    public void restore() {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.getInventory().clear();
        player.getInventory().setContents(inventory);
        player.getInventory().setArmorContents(armor);
        player.setExp(exp);
        player.teleport(oldLocation);
        player.setGameMode(gameMode);
        player.teleport(oldLocation);
    }

    public Player getPlayer() {
        return player;
    }
}
