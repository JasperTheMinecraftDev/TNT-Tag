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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TopStats {

    private final Player player;
    private final String type;
    private final Tnttag plugin = Tnttag.getInstance();

    public TopStats(Player player, String type) {
        this.player = player;
        this.type = type;
    }

    public void open() {
        PlayerData playerData = new PlayerData(player.getUniqueId());
        Map<UUID, Integer> topData;
        String topMessage;
        switch (type) {
            case "wins":
                topData = playerData.getWinsData();
                topMessage = ChatUtils.getRaw("top-gui.wins");
                break;
            case "timestagged":
                topData = playerData.getTimesTaggedData();
                topMessage = ChatUtils.getRaw("top-gui.timestagged");
                break;
            case "tags":
                topData = playerData.getTagsData();
                topMessage = ChatUtils.getRaw("top-gui.tags");
                break;
            default:
                ChatUtils.sendMessage(player, "general.invalid-stat-type");
                return;
        }
        List<Map.Entry<UUID, Integer>> topThreePlayers = topData.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .collect(Collectors.toList());
        if (topThreePlayers.isEmpty()) {
            ChatUtils.sendMessage(player, "general.not-enough-stats");
            return;
        }
        int rows = getRowSize(topThreePlayers.size());
        RyseInventory inventory = RyseInventory.builder()
                .title(ChatUtils.colorize(topMessage))
                .rows(rows)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        AtomicInteger position = new AtomicInteger(1);
                        topThreePlayers.forEach(entry -> {
                            UUID playerId = entry.getKey();
                            int playerStat = entry.getValue();
                            contents.set(position.getAndIncrement(), IntelligentItem.empty(new ItemBuilder(Material.PLAYER_HEAD).setSkullOwner(playerId).displayName(ChatUtils.colorize("&b" + Bukkit.getOfflinePlayer(playerId).getName() + "&6 - &b" + playerStat)).build()));
                        });
                        contents.fillEmpty(new ItemBuilder(XMaterial.valueOf(ChatUtils.getRaw("top-gui.emptySlotMaterial")).parseMaterial()).build());
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
