package nl.juriantech.tnttag.gui;

import com.cryptomorin.xseries.XMaterial;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerData;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Stats {

    private final Player player;
    private final Tnttag plugin;
    public Stats(Tnttag plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void open() {
        PlayerData playerData = new PlayerData(player.getUniqueId());
        RyseInventory inventory = RyseInventory.builder()
                .title(ChatUtils.colorize(ChatUtils.colorize(ChatUtils.getRaw("stats-gui.title"))))
                .rows(1)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        contents.set(3, IntelligentItem.empty(new ItemBuilder(Material.PAPER).displayName(ChatUtils.colorize(ChatUtils.getRaw("stats-gui.wins").replace("{wins}", String.valueOf(playerData.getWins())))).build()));
                        contents.set(4, IntelligentItem.empty(new ItemBuilder(Material.PAPER).displayName(ChatUtils.colorize(ChatUtils.getRaw("stats-gui.timestagged").replace("{timestagged}", String.valueOf(playerData.getTimesTagged())))).build()));
                        contents.set(5, IntelligentItem.empty(new ItemBuilder(Material.PAPER).displayName(ChatUtils.colorize(ChatUtils.getRaw("stats-gui.tags").replace("{tags}", String.valueOf(playerData.getTags())))).build()));

                        contents.fillEmpty(new ItemBuilder(XMaterial.valueOf(ChatUtils.getRaw("stats-gui.emptySlotMaterial")).parseMaterial()).build());
                    }
                })
                .build(plugin);
        inventory.open(player);
    }
}
