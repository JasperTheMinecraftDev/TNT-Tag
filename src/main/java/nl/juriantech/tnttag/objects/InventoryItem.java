package nl.juriantech.tnttag.objects;

import org.bukkit.inventory.ItemStack;

public class InventoryItem {

    private final String name;
    private final ItemStack item;
    private final String permission;

    public InventoryItem(String name, ItemStack item, String permission) {
        this.name = name;
        this.item = item;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getPermission() {
        return permission;
    }
}
