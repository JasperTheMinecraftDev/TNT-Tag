package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;

public class ItemManager {

    private final GameManager gameManager;

    public ItemManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    public void giveLobbyItems(Player player) {
        clearInv(player);
        if (player.hasPermission("tnttag.gui.join")) {
            player.getInventory().setItem(0,ItemBuilder.from(ChatUtils.getRaw("in-game-items.join")).build());
        }
        player.getInventory().setItem(8, ItemBuilder.from(ChatUtils.getRaw("in-game-items.leave")).build());
    }

    public void giveGameItems() {
        for (Player player : gameManager.playerManager.getPlayers().keySet()) {
            clearInv(player);
            player.getInventory().setItem(8, ItemBuilder.from(ChatUtils.getRaw("in-game-items.leave")).build());
        }
    }

    public void giveGameItems(Player player) {
        clearInv(player);
        player.getInventory().setItem(8, ItemBuilder.from(ChatUtils.getRaw("in-game-items.leave")).build());
    }

    public void giveTaggerItems(Player tagger) {
        giveGameItems(); //The inventory is already cleared here.
        tagger.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
        tagger.getInventory().setItem(0, new ItemStack(Material.TNT, 1));
        tagger.getInventory().setItem(7, ItemBuilder.from(ChatUtils.getRaw("in-game-items.radar")).build());
    }

    public void updateCompass(Player player) {
        ItemStack compass = player.getInventory().getItem(7);
        Player nearestPlayer = getNearestSurvivor(player);
        if (compass != null && nearestPlayer != null && compass.getItemMeta() != null) {
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName((ChatUtils.colorize("&6" + (int) player.getLocation().distance(nearestPlayer.getLocation()) + "m")));
            compass.setItemMeta(meta);
        }
    }

    public void clearInv(Player player) {
        player.getInventory().clear();
    }

    /**
     * Returns the nearest tagger to another player, or null if there is no other player in this world
     * @param player Player to check
     * @return Nearest other tagger, or null if there is no other player in this world
     */
    public Player getNearestSurvivor(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();
        ArrayList<Player> playersInWorld = new ArrayList<>(world.getEntitiesByClass(Player.class));
        if (playersInWorld.size() == 1) {
            return null;
        }

        playersInWorld.remove(player);
        playersInWorld.removeIf(p -> p != null && gameManager.playerManager.getPlayers().get(p).equals(PlayerType.SURVIVOR));
        playersInWorld.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(location)));
        return playersInWorld.isEmpty() ? null : playersInWorld.get(0);
    }
}
