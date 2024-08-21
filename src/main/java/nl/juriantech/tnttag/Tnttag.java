package nl.juriantech.tnttag;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.dejvokep.boostedyaml.YamlDocument;
import nl.juriantech.tnttag.api.API;
import nl.juriantech.tnttag.checkers.UpdateChecker;
import nl.juriantech.tnttag.handlers.SetupCommandHandler;
import nl.juriantech.tnttag.hooks.PartyAndFriendsHook;
import nl.juriantech.tnttag.hooks.PlaceholderAPIExpansion;
import nl.juriantech.tnttag.hooks.TabHook;
import nl.juriantech.tnttag.listeners.*;
import nl.juriantech.tnttag.managers.*;
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import nl.juriantech.tnttag.runnables.SignUpdateRunnable;
import nl.juriantech.tnttag.subcommands.*;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

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
    private TabHook tabHook;
    private EntityDamageByEntityListener entityDamageByEntityListener;
    private PlaceholderAPIExpansion placeholderAPIExpansion;
    private JoinSubCommand joinSubCommand;

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
        subcommands();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            logger.info("[TNT-Tag] PlaceholderAPI detected, enabling the hook.");
            placeholderAPIExpansion = new PlaceholderAPIExpansion(this);
            placeholderAPIExpansion.register();
            logger.info("[TNT-Tag] PlaceholderAPI hooks enabled.");
        }

        if (Bukkit.getPluginManager().getPlugin("PartyAndFriends") != null) {
            logger.info("[TNT-Tag] PartyAndFriends detected, enabling the hook.");
            this.partyAndFriendsHook = new PartyAndFriendsHook();
            logger.info("[TNT-Tag] PartyAndFriends hooks enabled.");
        }

        if (Bukkit.getPluginManager().getPlugin("TAB") != null) {
            logger.info("[TNT-Tag] TAB detected, enabling the hook.");
            this.tabHook = new TabHook(this);
            logger.info("[TNT-Tag] TAB hooks enabled.");
        }

        api = new API(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
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
        this.itemManager = new ItemManager(this);
        this.itemManager.load();
        this.lobbyManager = new LobbyManager(this);
        this.dumpManager = new DumpManager(this);
    }

    private void menuLibrary() {
        this.inventoryManager = new InventoryManager(this);
        this.inventoryManager.invoke();
    }

    private YamlDocument loadFile(String fileName) {
        try {
            return YamlDocument.create(new File(getDataFolder(), fileName), getResource(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void files() {
        arenasfile = loadFile("arenas.yml");
        customizationfile = loadFile("customization.yml");
        configfile = loadFile("config.yml");
        playerdatafile = loadFile("playerdata.yml");
        signsdatafile = loadFile("signs.yml");
        itemsfile = loadFile("items.yml");

        // We use a runnable, so it loads after the worlds.
        new BukkitRunnable() {
            @Override
            public void run() {
                arenaManager.loadArenasFromFile();
                if (Tnttag.configfile.getString("globalLobby") != null) {
                    lobbyManager.load();
                }
            }
        }.runTaskLater(this, 20);
    }

    private void subcommands() {
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);

        // Arena name resolver
        handler.registerValueResolver(Arena.class, context -> {
            String input = context.popForParameter();
            Arena arena = arenaManager.getArena(input);
            if (arena == null) {
                throw new CommandErrorException(ChatUtils.colorize(ChatUtils.getRaw("commands.invalid-arena")));
            }
            return arena;
        });

        handler.register(new CreateSubCommand(this));
        handler.register(new DeleteSubCommand(this));
        handler.register(new DumpSubCommand(this));
        handler.register(new EditorSubCommand(this));
        handler.register(new HelpSubCommand());
        handler.register(new InfoSubCommand(this));
        handler.register(new JoinGUISubCommand(this));
        joinSubCommand = new JoinSubCommand(this);
        handler.register(joinSubCommand);
        handler.register(new LeaveSubCommand(this));
        handler.register(new ListSubCommand(this));
        handler.register(new ReloadSubCommand(this));
        handler.register(new SetLobbySubCommand(this));
        handler.register(new StartSubCommand(this));
        handler.register(new StatsSubCommand(this));
        handler.register(new TopSubCommand(this));
        handler.register(new RandomJoinSubCommand(this));
        handler.register(new ForceJoinSubCommand(this));

        handler.registerBrigadier();
    }

    private void listeners() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
        entityDamageByEntityListener = new EntityDamageByEntityListener(this);
        getServer().getPluginManager().registerEvents(entityDamageByEntityListener, this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
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

    public void connectToServer(Player player, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(serverName);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
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

    public TabHook getTabHook() {
        return tabHook;
    }

    public PlaceholderAPIExpansion getPlaceholderAPIExpansion() {
        return placeholderAPIExpansion;
    }

    public EntityDamageByEntityListener getEntityDamageByEntityListener() {
        return entityDamageByEntityListener;
    }

    public JoinSubCommand getJoinSubCommand() {
        return joinSubCommand;
    }
}