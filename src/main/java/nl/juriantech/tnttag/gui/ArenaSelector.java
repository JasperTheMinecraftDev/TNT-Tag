package nl.juriantech.tnttag.gui;

import com.cryptomorin.xseries.XMaterial;
import io.github.rysefoxx.inventory.plugin.content.IntelligentItem;
import io.github.rysefoxx.inventory.plugin.content.InventoryContents;
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider;
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory;
import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.ArenaManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import nl.juriantech.tnttag.utils.ItemBuilder;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ArenaSelector {

    private final Player player;
    private final Tnttag plugin = Tnttag.getInstance();
    private final ArenaManager arenaManager = plugin.getArenaManager();

    public ArenaSelector(Player player) {
        this.player = player;
    }

    public void open() {
        int arenaCount = arenaManager.getArenaObjectsSize();
        int rows = getRowSize(arenaCount);
        ArrayList<Arena> arenaObjects = new ArrayList<>(arenaManager.getArenaObjects());
        RyseInventory inventory = RyseInventory.builder()
                .title(ChatUtils.colorize(ChatUtils.getRaw("join-gui.title").replace("{count}", String.valueOf(arenaCount))))
                .rows(rows)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        int slot = 0;
                        for (Arena arena : arenaObjects) {
                            String displayName = ChatUtils.colorize(ChatUtils.getRaw("join-gui.arenaTitle").replace("{name}", arena.getName()).replace("{state}", arena.getGameManager().state.toString()));
                            contents.set(slot, IntelligentItem.of(new ItemBuilder(XMaterial.valueOf(ChatUtils.getRaw("join-gui.arenaMaterial")).parseMaterial()).displayName(displayName).lore(ChatUtils.getRaw("join-gui.arenaLore")).hideAttributes().build(), event -> {
                                player.performCommand("tnttag join " + arena.getName());
                                player.closeInventory();
                            }));
                            slot++;
                        }
                        contents.fillEmpty(new ItemBuilder(XMaterial.valueOf(ChatUtils.getRaw("join-gui.emptySlotMaterial")).parseMaterial()).build());
                    }
                })
                .build(plugin);
        inventory.open(player);
    }

    private int getRowSize(int arenaCount) {
        if (arenaCount <= 8) {
            return 1;
        } else if (arenaCount <= 16) {
            return 2;
        } else if (arenaCount <= 24) {
            return 3;
        } else {
            return 4;
        }
    }
}
