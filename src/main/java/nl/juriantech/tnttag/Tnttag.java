package nl.juriantech.tnttag;

import dev.dejvokep.boostedyaml.YamlDocument;
import nl.juriantech.tnttag.checkers.UpdateChecker;
import nl.juriantech.tnttag.commands.TnttagCommand;
import nl.juriantech.tnttag.handlers.SetupCommandHandler;
import nl.juriantech.tnttag.hooks.PlaceholderAPIExpansion;
import nl.juriantech.tnttag.listeners.*;
import nl.juriantech.tnttag.managers.ArenaManager;
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.runnables.SignUpdateRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Tnttag extends JavaPlugin {

    private static Tnttag instance;
    private final Logger logger = Bukkit.getLogger();
    private ArenaManager arenaManager;
    public static YamlDocument arenasfile, customizationfile, configfile, playerdatafile, signsdatafile;
    private UpdateChecker updateChecker;
    private SignManager signManager;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable() {
        instance = this;
        new SetupCommandHandler();
        updateChecker = new UpdateChecker();
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
        arenaManager = new ArenaManager(this);
        this.signManager = new SignManager(this);
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
        handler.register(new TnttagCommand());
    }

    private void listeners() {
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(updateChecker, this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerItemDropListener(), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new ItemListener(), this);
    }

    @Override
    public void onDisable() {
        arenaManager.endAllArenas();
        signManager.saveSigns();
        arenaManager.saveArenasToFile();
        instance = null;
        logger.severe("TNT-Tag has been disabled!");
    }

    public static Tnttag getInstance() {
        return instance;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SignManager getSignManager() {
        return signManager;
    }
}