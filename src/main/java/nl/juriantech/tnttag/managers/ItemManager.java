package nl.juriantech.tnttag.managers;

import com.cryptomorin.xseries.XMaterial;
import dev.dejvokep.boostedyaml.route.Route;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.InventoryItem;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemManager {

    private final HashMap<Integer, InventoryItem> globalLobbyItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> waitingItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> gameItems = new HashMap<>();
    private final HashMap<Integer, InventoryItem> taggerItems = new HashMap<>();

    public void giveGlobalLobbyItems(Player player) {
        clearInv(player);
        for (InventoryItem item : globalLobbyItems.values()) {
            if (item.getPermission().equals("NONE") || player.hasPermission(item.getPermission())) {
                player.getInventory().addItem(item.getItem());
            }
        }
    }

    public void giveWaitingItems(Player player) {
        clearInv(player);
        for (InventoryItem item : waitingItems.values()) {
            if (item.getPermission().equals("NONE") || player.hasPermission(item.getPermission())) {
                player.getInventory().addItem(item.getItem());
            }
        }
    }

    public void giveGameItems(Player player) {
        clearInv(player);
        for (InventoryItem item : gameItems.values()) {
            if (item.getPermission().equals("NONE") || player.hasPermission(item.getPermission())) {
                player.getInventory().addItem(item.getItem());
            }
        }
    }

    public void giveTaggerItems(Player tagger) {
        giveGameItems(tagger); //The inventory is already cleared here.
        tagger.getInventory().setHelmet(new ItemStack(Material.TNT, 1)); //This is forced.
        for (InventoryItem item : taggerItems.values()) {
            if (item.getPermission().equals("NONE") || tagger.hasPermission(item.getPermission())) {
                tagger.getInventory().addItem(item.getItem());
            }

        }
    }

    public void clearInv(Player player) {
        player.getInventory().clear();
    }

    public void load() {
        ArrayList<InventoryItem> items = new ArrayList<>();
        for (Route route : Tnttag.itemsfile.getSection("items").getRoutes(false)) {
            items.add(new InventoryItem(route.toString(), new ItemBuilder(XMaterial.valueOf(Tnttag.itemsfile.getString(route.toString() + ".material")).parseMaterial()).displayName(Tnttag.itemsfile.getString(route.toString() + ".display_name")).lore(Tnttag.itemsfile.getString(route.toString() + ".lore")).build(), Tnttag.itemsfile.getString(route.toString() + ".permission")));
        }

        for (String itemString: Tnttag.itemsfile.getStringList("globalLobbyItems")) {
            String[] parts = itemString.split(":");
            int slot = Integer.parseInt(parts[0]);
            InventoryItem inventoryItem = items.stream()
                    .filter(inventoryItem1 -> inventoryItem1.getName().equals(parts[1]))
                    .findFirst()
                    .orElse(null);

            globalLobbyItems.put(slot, inventoryItem);
        }

        for (String itemString: Tnttag.itemsfile.getStringList("waitingItems")) {
            String[] parts = itemString.split(":");
            int slot = Integer.parseInt(parts[0]);
            InventoryItem inventoryItem = items.stream()
                    .filter(inventoryItem1 -> inventoryItem1.getName().equals(parts[1]))
                    .findFirst()
                    .orElse(null);

            waitingItems.put(slot, inventoryItem);
        }

        for (String itemString: Tnttag.itemsfile.getStringList("gameItems")) {
            String[] parts = itemString.split(":");
            int slot = Integer.parseInt(parts[0]);
            InventoryItem inventoryItem = items.stream()
                    .filter(inventoryItem1 -> inventoryItem1.getName().equals(parts[1]))
                    .findFirst()
                    .orElse(null);

            gameItems.put(slot, inventoryItem);
        }

        for (String itemString: Tnttag.itemsfile.getStringList("taggerItems")) {
            String[] parts = itemString.split(":");
            int slot = Integer.parseInt(parts[0]);
            InventoryItem inventoryItem = items.stream()
                    .filter(inventoryItem1 -> inventoryItem1.getName().equals(parts[1]))
                    .findFirst()
                    .orElse(null);

            taggerItems.put(slot, inventoryItem);
        }
    }

    public void reload() {
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
}
