package nl.juriantech.tnttag.runnables;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.GameState;
import nl.juriantech.tnttag.managers.GameManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class StartRunnable extends BukkitRunnable {

    private GameManager gameManager;
    private List<Integer> timesToBroadcast;
    private int timeLeft;

    public StartRunnable(GameManager gameManager) {
        this.timeLeft = gameManager.arena.getCountdown();
        this.gameManager = gameManager;
        this.timesToBroadcast = new ArrayList<>();
        for (String time : Tnttag.configfile.getStringList("timesToBroadcast")) {
            timesToBroadcast.add(Integer.parseInt(time));
        }
    }

    @Override
    public void run() {
        if (timeLeft <= 0) {
            cancel();
            gameManager.setGameState(GameState.INGAME);
            return;
        }

        if (timesToBroadcast.contains(timeLeft)) {
            gameManager.playerManager.broadcast(ChatUtils.getRaw("arena.countdown-message").replace("{seconds}", String.valueOf(timeLeft)));
            for (Player player : gameManager.playerManager.getPlayers().keySet()) {
                player.setLevel(timeLeft);
                ChatUtils.sendTitle(player, "titles.countdown", 20, 20, 20, timeLeft);
            }
        }

        timeLeft --;
    }
}