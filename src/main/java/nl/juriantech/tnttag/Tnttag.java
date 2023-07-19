package nl.juriantech.tnttag;

import dev.dejvokep.boostedyaml.YamlDocument;
import nl.juriantech.tnttag.api.API;
import nl.juriantech.tnttag.checkers.UpdateChecker;
import nl.juriantech.tnttag.commands.TnttagCommand;
import nl.juriantech.tnttag.handlers.SetupCommandHandler;
import nl.juriantech.tnttag.hooks.PartyAndFriendsHook;
import nl.juriantech.tnttag.hooks.PlaceholderAPIExpansion;
import nl.juriantech.tnttag.listeners.*;
import nl.juriantech.tnttag.managers.*;
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import nl.juriantech.tnttag.runnables.SignUpdateRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tnttag extends JavaPlugin {

    private final Logger logger = Bukkit.getLogger();
    private ArenaManager arenaManager;
    public static YamlDocument arenasfile, customizationfile, configfile, playerdatafile, signsdatafile, itemsfile;
    private UpdateChecker updateChecker;
    private SignManager signManager;
    private InventoryManager inventoryManager;
    private PartyAndFriendsHook partyAndFriendsHook;
    private static API api;
    private LobbyManager lobbyManager;
    private ItemManager itemManager;
    private DumpManager dumpManager;

    @Override
    public void onEnable() {
        String version = Bukkit.getVersion();
        String minecraftVersion = getMinecraftVersion(version);

        // Check if the server version is 1.12.2 or below
        if (minecraftVersion != null && isVersionBefore("1.12.3", minecraftVersion)) {
            getLogger().severe("TNT-Tag is not compatible with Minecraft 1.12.2 or lower and you're running " + minecraftVersion + ". Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new SetupCommandHandler(this);
        updateChecker = new UpdateChecker(this);
        updateChecker.check();
        files();
        menuLibrary();
        managers();
        runnables();
        listeners();
        commands();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("[TNT-Tag] PlaceholderAPI detected, enabling hooks.");
            new PlaceholderAPIExpansion(this).register();
            logger.info("[TNT-Tag] PlaceholderAPI hooks enabled.");
        }

        if (Bukkit.getPluginManager().getPlugin("PartyAndFriends") != null) {
            logger.info("[TNT-Tag] PartyAndFriends detected, enabling hooks.");
            this.partyAndFriendsHook = new PartyAndFriendsHook();
            logger.info("[TNT-Tag] PartyAndFriends hooks enabled.");
        }
        api = new API(this);
        logger.warning("TNT-Tag has been enabled!");
        new BukkitRunnable() {
            @Override
            public void run() {
                signManager.loadSigns();
            }
        }.runTaskLater(this, 20L);
    }


    private void runnables() {
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new SignUpdateRunnable(this), 0L, 200L);
    }

    private void managers() {
        this.arenaManager = new ArenaManager(this);
        this.signManager = new SignManager(this);
        this.itemManager = new ItemManager();
        this.itemManager.load();
        this.lobbyManager = new LobbyManager(this);
        this.dumpManager = new DumpManager(this);
    }

    private void menuLibrary() {
        this.inventoryManager = new InventoryManager(this);
        this.inventoryManager.invoke();
    }

    private void files() {
        try {
            arenasfile = YamlDocument.create(new File(getDataFolder(), "arenas.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            customizationfile = YamlDocument.create(new File(getDataFolder(), "customization.yml"), getResource("customization.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            configfile = YamlDocument.create(new File(getDataFolder(), "config.yml"), getResource("config.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            playerdatafile = YamlDocument.create(new File(getDataFolder(), "playerdata.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            signsdatafile = YamlDocument.create(new File(getDataFolder(), "signs.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            itemsfile = YamlDocument.create(new File(getDataFolder(), "items.yml"), getResource("items.yml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // We use a runnable, so it loads after the worlds.
        new BukkitRunnable() {
            @Override
            public void run() {
                arenaManager.loadArenasFromFile();
            }
        }.runTaskLater(this, 20);
    }

    private void commands() {
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);
        handler.register(new TnttagCommand(this));
    }

    private void listeners() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
    }

    @Override
    public void onDisable() {
        arenaManager.endAllArenas();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (lobbyManager.playerIsInLobby(player)) {
                lobbyManager.leaveLobby(player);
            }
        }
        signManager.saveSigns();
        arenaManager.saveArenasToFile();
        logger.severe("TNT-Tag has been disabled!");
    }

    // Helper method to extract Minecraft version from version string
    private String getMinecraftVersion(String versionString) {
        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(versionString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isVersionBefore(String targetVersion, String currentVersion) {
        String[] targetParts = targetVersion.split("\\.");
        String[] currentParts = currentVersion.split("\\.");

        for (int i = 0; i < targetParts.length && i < currentParts.length; i++) {
            int targetPart = Integer.parseInt(targetParts[i]);
            int currentPart = Integer.parseInt(currentParts[i]);

            if (currentPart < targetPart) {
                return true;
            } else if (currentPart > targetPart) {
                return false;
            }
            // If the parts are equal, continue to the next part
        }

        return false; // The versions are equal or the current version is greater
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }

    public PartyAndFriendsHook getPartyAndFriendsHook() {
        return partyAndFriendsHook;
    }

    public static API getAPI() {
        return api;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    public ItemManager getItemManager() {
        return this.itemManager;
    }

    public DumpManager getDumpManager() {
        return dumpManager;
    }
}