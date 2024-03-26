package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.InventoryItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {

    private final Tnttag plugin;

    public ItemListener(Tnttag plugin) {
        this.plugin = plugin;
    }

    private boolean isCustomItem(ItemStack itemStack) {
        for (InventoryItem item : plugin.getItemManager().getItems()) {
            if (itemStack.equals(item.getItem())) return true;
        }
        return false;
    }

    private String getCommandForCustomItem(ItemStack itemStack) {
        InventoryItem inventoryItem = null;

        for (InventoryItem item : plugin.getItemManager().getItems()) {
            if (itemStack.equals(item.getItem())) inventoryItem = item;
        }

        if (inventoryItem != null) return inventoryItem.getCommand();
        return null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == null) return;

        Player player = event.getPlayer();
        if (!plugin.getLobbyManager().playerIsInLobby(player)) return;

        ItemStack itemInHand = player.getInventory().getItem(event.getHand());
        if (itemInHand.getType() == Material.AIR) return;

        if (isCustomItem(itemInHand)) {
            String command = getCommandForCustomItem(itemInHand);
            if (command != null && !command.isEmpty() && !command.equals("NONE")) {
                player.performCommand(command);
                event.setCancelled(true);
            }
        }
    }
}