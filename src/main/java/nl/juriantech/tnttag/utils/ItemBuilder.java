package nl.juriantech.tnttag.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This is a chainable builder for {@link ItemStack}s in {@link Bukkit}
 * <br>
 * Example Usage:<br>
 * {@code ItemStack is = new ItemBuilder(Material.LEATHER_HELMET).amount(2).data(4).durability(4).enchantment(Enchantment.ARROW_INFINITE).enchantment(Enchantment.LUCK, 2).name(ChatColor.RED + "the name").lore(ChatColor.GREEN + "line 1").lore(ChatColor.BLUE + "line 2").color(Color.MAROON).build();}
 *
 * @author MiniDigger
 * @version 1.2
 */
public class ItemBuilder {
    private final ItemStack item;

    private String name;
    private boolean hideAttributes;
    private List<String> lore = new ArrayList<>();
    private UUID skullOwner;

    /**
     * Creates a new ItemBuilder with the specified material.
     *
     * @param material The material
     */
    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount Item amount
     * @return This ItemBuilder
     */
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Sets the name of the item.
     *
     * @param name Item name
     * @return This ItemBuilder
     */
    public ItemBuilder displayName(String name) {
        this.name = ChatUtils.colorize(name);
        return this;
    }

    /**
     * Adds an empty lore line.
     * This can be used to skip a line,
     * a.k.a. a spacer.
     *
     * @return This ItemBuilder
     */
    public ItemBuilder addLoreSpacer() {
        this.lore.add("");
        return this;
    }

    /**
     * Adds the specified lore line to the
     * item. If there are newline characters
     * in the string, multiple lines are added.
     *
     * @param lore Line to add to the lore.
     * @return This ItemBuilder
     */
    public ItemBuilder lore(String lore) {
        String[] lores = lore.split("\n");
        for(String splitLore : lores) {
            this.lore.add(ChatUtils.colorize(splitLore));
        }
        return this;
    }

    /**
     * Sets the skull owner of the item to the specified player.
     *
     * @param player The player's name or UUID
     * @return This ItemBuilder
     */
    public ItemBuilder setSkullOwner(UUID player) {
        this.skullOwner = player;
        return this;
    }

    /**
     * Hides this item's attributes, like damage
     * stats on swords.
     *
     * @return This ItemBuilder
     */
    public ItemBuilder hideAttributes() {
        this.hideAttributes = true;
        return this;
    }

    /**
     * Returns an ItemStack with the settings
     * specified in this ItemBuilder.
     * <p>
     * This returns a clone, so modifying
     * the ItemStack further afterwards does
     * not affect this ItemBuilder.
     *
     * @return A clone of the ItemStack
     */
    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(name);
            }
            if (lore != null) {
                meta.setLore(lore);
            }

            if (hideAttributes) {
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            item.setItemMeta(meta);

            if (skullOwner != null) {
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(skullOwner));
                item.setItemMeta(meta);
            }
        }
            return item.clone();
    }
}