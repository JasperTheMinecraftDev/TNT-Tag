package nl.juriantech.tnttag.managers;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.PlayerType;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreboardManager {

    private final GameManager gameManager;
    private org.bukkit.scoreboard.ScoreboardManager scoreboardManager;
    private Scoreboard scoreboard;
    private Objective objective;
    private final YamlDocument config;

    public ScoreboardManager(Tnttag plugin, GameManager gameManager, YamlDocument config) {
        this.gameManager = gameManager;
        this.config = config;

        // Schedule a synchronous task to initialize the scoreboard on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                scoreboardManager = Bukkit.getScoreboardManager();
                scoreboard = scoreboardManager.getNewScoreboard();
                objective = scoreboard.registerNewObjective("TNTTagStats", "dummy", ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard.title")));
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
        }.runTask(plugin);  // Run this synchronously

        new BukkitRunnable() {
            @Override
            public void run() {
                update();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public void apply() {
        for (Player player : gameManager.playerManager.getPlayers().keySet()) {
            showScoreboard(player);
        }
    }

    public void remove() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getScoreboard().equals(scoreboard)) {
                player.setScoreboard(scoreboardManager.getNewScoreboard()); // Set empty scoreboard
            }
        }
    }

    public void update() {
        // Reset existing scores
        scoreboard.getEntries().forEach(scoreboard::resetScores);

        int line = config.getStringList("scoreboard.lines").size();
        for (String lineText : config.getStringList("scoreboard.lines")) {
            String processedLine = replacePlaceholders(lineText);
            addLine(processedLine, line--); // Add lines from bottom up
        }
    }

    private String replacePlaceholders(String text) {
        text = text.replace("%state%", gameManager.getCustomizedState())
                   .replace("%currentPlayers%", String.valueOf(gameManager.playerManager.getPlayers().size()))
                   .replace("%maxPlayers%", String.valueOf(gameManager.arena.getMaxPlayers()))
                   .replace("%taggers%", String.valueOf(gameManager.playerManager.getPlayers().entrySet().stream().filter(playerPlayerTypeEntry -> playerPlayerTypeEntry.getValue() == PlayerType.TAGGER).count()))
                   .replace("%survivors%", String.valueOf(gameManager.playerManager.getPlayers().entrySet().stream().filter(playerPlayerTypeEntry -> playerPlayerTypeEntry.getValue() == PlayerType.SURVIVOR).count()))
                   .replace("%spectators%", String.valueOf(gameManager.playerManager.getPlayers().entrySet().stream().filter(playerPlayerTypeEntry -> playerPlayerTypeEntry.getValue() == PlayerType.SPECTATOR).count()))
                   .replace("%time%", gameManager.round == null ? "N/A" : String.valueOf(gameManager.round.getRoundDuration()))
                   .replace("%name%", gameManager.arena.getName());

        text = text.replace("%date%", getCurrentTime());
        return ChatUtils.colorize(text);
    }

    private String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
        Date date = new java.util.Date();
        return formatter.format(date);
    }

    private void showScoreboard(Player player) {
        player.setScoreboard(scoreboard);
    }

    private void addLine(String text, int score) {
        if (text.isEmpty()) return; // Skip empty lines
        Score lineScore = objective.getScore(text);
        lineScore.setScore(score);
    }
}
