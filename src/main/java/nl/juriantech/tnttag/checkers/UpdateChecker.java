package nl.juriantech.tnttag.checkers;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {

    private static final String UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=105832";

    private final String localVersion;
    private String onlineVersion;
    private boolean isAvailable;

    public UpdateChecker() {
        localVersion = Tnttag.getInstance().getDescription().getVersion();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("tnttag.update"))
            check();
        if (isAvailable) {
                player.sendMessage(ChatUtils.colorize("&b================&cTNT-Tag&b================"));
                player.sendMessage(ChatUtils.colorize("&cTNT-Tag >>  &fNew version available: &b" + onlineVersion));
                player.sendMessage(ChatUtils.colorize("&cTNT-Tag >>  &fCurrent version: &b" + localVersion));
                player.sendMessage(ChatUtils.colorize("&b========================================"));
            }
    }

    public void check() {
        isAvailable = checkUpdate();
    }

    private boolean checkUpdate() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(UPDATE_URL).openConnection();
            connection.setRequestMethod("GET");
            String raw = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            onlineVersion = raw.split("-")[0].trim();
            return !localVersion.equalsIgnoreCase(onlineVersion);
        } catch (IOException e) {
            return false;
        }
    }

}
