package nl.juriantech.tnttag.managers;

import com.cryptomorin.xseries.XMaterial;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.InventoryItem;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemManager {

    private final Tnttag plugin;
    private final ArrayList<InventoryItem> items = new ArrayList<>();
    private final HashMap<Integer, InventoryItem> globalLobbyItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> waitingItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> gameItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> taggerItems = new HashMap<>();

    public ItemManager(Tnttag plugin) {
        this.plugin = plugin;
    }

    public void giveGlobalLobbyItems(Player player) {
        clearInv(player);
        for (Map.Entry<Integer, InventoryItem> item : globalLobbyItems.entrySet()) {
            if (item != null && (item.getValue().getPermission().equals("NONE") || player.hasPermission(item.getValue().getPermission()))) {
                player.getInventory().setItem(item.getKey(), item.getValue().getItem());
            }
        }
    }

    public void giveWaitingItems(Player player) {
        clearInv(player);
        for (Map.Entry<Integer, InventoryItem> item : waitingItems.entrySet()) {
            if (item != null && (item.getValue().getPermission().equals("NONE") || player.hasPermission(item.getValue().getPermission()))) {
                player.getInventory().setItem(item.getKey(), item.getValue().getItem());
            }
        }
    }

    public void giveGameItems(Player player) {
        clearInv(player);
        for (Map.Entry<Integer, InventoryItem> item : gameItems.entrySet()) {
            if (item != null && (item.getValue().getPermission().equals("NONE") || player.hasPermission(item.getValue().getPermission()))) {
                player.getInventory().setItem(item.getKey(), item.getValue().getItem());
            }
        }
    }

    public void giveTaggerItems(Player tagger) {
        giveGameItems(tagger); // The inventory is already cleared here.
        tagger.getInventory().setHelmet(new ItemStack(Material.TNT, 1)); // This is forced.
        for (Map.Entry<Integer, InventoryItem> item : taggerItems.entrySet()) {
            if (item != null && (item.getValue().getPermission().equals("NONE") || tagger.hasPermission(item.getValue().getPermission()))) {
                tagger.getInventory().setItem(item.getKey(), item.getValue().getItem());
            }
        }
    }

    public void clearInv(Player player) {
        player.getInventory().clear();
    }

    public void load() {
        for (String route : Tnttag.itemsfile.getRoutesAsStrings(true)) {
            // If the route starts with "items." and has exactly one dot in it
            if (route.startsWith("items.") && route.substring("items.".length()).indexOf('.') == -1) {
                String name = route.replace("items.", "");
                if (getItemByName(name) == null) {
                    InventoryItem inventoryItem = new InventoryItem(name, new ItemBuilder(XMaterial.valueOf(Tnttag.itemsfile.getString(route + ".material")).parseMaterial()).displayName(Tnttag.itemsfile.getString(route + ".display_name")).lore(Tnttag.itemsfile.getString(route + ".lore")).build(), Tnttag.itemsfile.getString(route + ".permission"), Tnttag.itemsfile.getString(route + ".command"));
                    items.add(inventoryItem);
                }
            }
        }

        loadItemsForSection("globalLobbyItems", globalLobbyItems);
        loadItemsForSection("waitingItems", waitingItems);
        loadItemsForSection("gameItems", gameItems);
        loadItemsForSection("taggerItems", taggerItems);
    }

    private void loadItemsForSection(String sectionName, HashMap<Integer, InventoryItem> targetMap) {
        List<String> itemStrings = Tnttag.itemsfile.getStringList(sectionName);
        for (String itemString : itemStrings) {
            String[] parts = itemString.split(":");
            if (parts.length < 2) {
                plugin.getLogger().severe("Invalid " + sectionName + " entry: " + itemString);
                continue;
            }
            int slot = Integer.parseInt(parts[0]);
            String itemName = parts[1];
            InventoryItem inventoryItem = getItemByName(itemName);

            if (inventoryItem == null) {
                plugin.getLogger().severe("Failed to find InventoryItem for " + sectionName + ", itemString: " + itemString);
            } else {
                targetMap.put(slot, inventoryItem);
            }
        }
    }

    public void reload() {
        items.clear();
        globalLobbyItems.clear();
        waitingItems.clear();
        gameItems.clear();
        taggerItems.clear();

        try {
            Tnttag.itemsfile.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        load();
    }

    private InventoryItem getItemByName(String name) {
        return items.stream()
                .filter(item -> item.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public ArrayList<InventoryItem> getItems() {
        return items;
    }
}
