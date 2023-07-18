package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemManager {

    public void giveGlobalLobbyItems(Player player) {
        clearInv(player);
        if (player.hasPermission("tnttag.gui.join")) {
            player.getInventory().setItem(0,ItemBuilder.from(ChatUtils.getRaw("items.join")).build());
        }
        player.getInventory().setItem(8, ItemBuilder.from(ChatUtils.getRaw("items.leave")).build());
    }

    public void giveWaitingAndGameItems(Player player) {
        clearInv(player);
        player.getInventory().setItem(8, ItemBuilder.from(ChatUtils.getRaw("items.leave")).build());
    }

    public void giveTaggerItems(Player tagger) {
        giveWaitingAndGameItems(tagger); //The inventory is already cleared here.
        tagger.getInventory().setHelmet(new ItemStack(Material.TNT, 1));
        tagger.getInventory().setItem(0, new ItemStack(Material.TNT, 1));
        tagger.getInventory().setItem(7, ItemBuilder.from(ChatUtils.getRaw("items.radar")).build());
    }

    public void clearInv(Player player) {
        player.getInventory().clear();
    }
}
