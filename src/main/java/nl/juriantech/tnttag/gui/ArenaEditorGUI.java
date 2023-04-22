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
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;

public class ArenaEditorGUI {

    private final Player player;
    private final Arena arena;
    private final Tnttag plugin;
    private final ArenaManager arenaManager;

    public ArenaEditorGUI(Tnttag plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        this.player = player;
        this.arena = arena;
    }

    public void open() {
        RyseInventory inventory = RyseInventory.builder()
                .title(ChatUtils.colorize(ChatUtils.getRaw("editor-gui.title")).replace("{arena}", arena.getName()))
                .rows(3)
                .provider(new InventoryProvider() {
                    @Override
                    public void init(Player player, InventoryContents contents) {
                        //minPlayers
                        contents.set(1, IntelligentItem.of(new ItemBuilder(Material.GRAY_DYE).displayName(ChatUtils.getRaw("editor-gui.decrement")).hideAttributes().build(), event -> {
                            int newAmount = arena.getMinPlayers() - 1;
                            if (newAmount <= 0) {
                                ChatUtils.sendMessage(player, "general.negative-error");
                                return;
                            }

                            if (newAmount < 2) {
                                ChatUtils.sendMessage(player, "setup.minPlayers-too-low");
                                return;
                            }

                            arena.setMinPlayers(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        contents.set(10, IntelligentItem.empty(new ItemBuilder(Material.LIGHT_WEIGHTED_PRESSURE_PLATE).displayName("&6Minimum players: " + arena.getMinPlayers()).lore(ChatUtils.getRaw("editor-gui.minPlayersLore")).setAmount(arena.getMinPlayers()).hideAttributes().build()));

                        contents.set(19, IntelligentItem.of(new ItemBuilder(Material.LIME_DYE).displayName(ChatUtils.getRaw("editor-gui.increment")).hideAttributes().build(), event -> {
                            int newAmount = arena.getMinPlayers() + 1;
                            arena.setMinPlayers(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //maxPlayers
                        contents.set(2, IntelligentItem.of(new ItemBuilder(Material.GRAY_DYE).displayName(ChatUtils.getRaw("editor-gui.decrement")).hideAttributes().build(), event -> {
                            int newAmount = arena.getMaxPlayers() - 1;
                            if (newAmount <= 0) {
                                ChatUtils.sendMessage(player, "general.negative-error");
                                return;
                            }

                            arena.setMaxPlayers(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        contents.set(11, IntelligentItem.empty(new ItemBuilder(Material.HEAVY_WEIGHTED_PRESSURE_PLATE).displayName("&6Maximum players: " + arena.getMaxPlayers()).lore(ChatUtils.getRaw("editor-gui.maxPlayersLore")).setAmount(arena.getMaxPlayers()).hideAttributes().build()));

                        contents.set(20, IntelligentItem.of(new ItemBuilder(Material.LIME_DYE).displayName(ChatUtils.getRaw("editor-gui.increment")).hideAttributes().build(), event -> {
                            int newAmount = arena.getMaxPlayers() + 1;
                            arena.setMaxPlayers(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //lobbyLocation
                        contents.set(12, IntelligentItem.of(new ItemBuilder(Material.ITEM_FRAME).displayName("&6Lobby location").lore(ChatUtils.getRaw("editor-gui.lobbyLocationLore")).hideAttributes().build(), event -> {
                            arena.setLobbyLocation(player.getLocation());
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //startLocation
                        contents.set(13, IntelligentItem.of(new ItemBuilder(Material.BEACON).displayName("&6Start location").lore(ChatUtils.getRaw("editor-gui.startLocationLore")).hideAttributes().build(), event -> {
                            arena.setStartLocation(player.getLocation());
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //countdown
                        contents.set(5, IntelligentItem.of(new ItemBuilder(Material.GRAY_DYE).displayName(ChatUtils.getRaw("editor-gui.decrement")).hideAttributes().build(), event -> {
                            int newAmount = arena.getCountdown() - 1;
                            if (newAmount <= 0) {
                                ChatUtils.sendMessage(player, "general.negative-error");
                                return;
                            }

                            arena.setCountdown(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        contents.set(14, IntelligentItem.empty(new ItemBuilder(Material.DAYLIGHT_DETECTOR).displayName("&6Countdown: " + arena.getCountdown()).lore(ChatUtils.getRaw("editor-gui.countdownLore")).setAmount(arena.getCountdown()).hideAttributes().build()));

                        contents.set(23, IntelligentItem.of(new ItemBuilder(Material.LIME_DYE).displayName(ChatUtils.getRaw("editor-gui.increment")).hideAttributes().build(), event -> {
                            int newAmount = arena.getCountdown() + 1;
                            arena.setCountdown(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //roundDuration
                        contents.set(6, IntelligentItem.of(new ItemBuilder(Material.GRAY_DYE).displayName(ChatUtils.getRaw("editor-gui.decrement")).hideAttributes().build(), event -> {
                            int newAmount = arena.getRoundDuration() - 1;
                            if (newAmount <= 0) {
                                ChatUtils.sendMessage(player, "general.negative-error");
                                return;
                            }
                            arena.setRoundDuration(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        contents.set(15, IntelligentItem.empty(new ItemBuilder(Material.HOPPER).displayName("&6RoundDuration: " + arena.getRoundDuration()).lore(ChatUtils.getRaw("editor-gui.roundDurationLore")).setAmount(arena.getRoundDuration()).hideAttributes().build()));

                        contents.set(24, IntelligentItem.of(new ItemBuilder(Material.LIME_DYE).displayName(ChatUtils.getRaw("editor-gui.increment")).hideAttributes().build(), event -> {
                            int newAmount = arena.getRoundDuration() + 1;
                            arena.setRoundDuration(newAmount);
                            arenaManager.saveArenaToFile(arena);
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            open();
                        }));

                        //apply button to apply the changes
                        contents.set(16, IntelligentItem.of(new ItemBuilder(Material.LEVER).displayName("&6Apply changes").build(), event -> {
                            try {
                                arenaManager.reload();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            ChatUtils.sendMessage(player, "editor-gui.hint");
                            player.closeInventory();
                        }));

                        contents.fillEmpty(new ItemBuilder(XMaterial.valueOf(ChatUtils.getRaw("editor-gui.emptySlotMaterial")).parseMaterial()).build());
                    }
                })
                .build(plugin);
        inventory.open(player);
    }
}
