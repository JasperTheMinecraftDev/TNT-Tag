package nl.juriantech.tnttag.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.objects.PlayerData;
import org.bukkit.entity.Player;

public class ChatUtils {

    public static String colorize(final String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(final Player player, final String path) {
        if (Tnttag.customizationfile.getString(path).isEmpty()) return;
        PlayerData playerData = new PlayerData(player.getUniqueId());
        String message = Tnttag.customizationfile.getString(path).replace("{player}", player.getName()).replace("{wins}", String.valueOf(playerData.getWins())).replace("{timestagged}", String.valueOf(playerData.getTimesTagged())).replace("{tags}", String.valueOf(playerData.getTags()));
        player.sendMessage(colorize(message));
    }

    public static void sendMessage(final Arena arena, final Player player, final String path) {
        if (Tnttag.customizationfile.getString(path).isEmpty()) return;

        String message = Tnttag.customizationfile.getString(path).replace("{player}", player.getName()).replace("{arena}", arena.getName());
        player.sendMessage(colorize(message));
    }

    public static void sendTitle(final Player player, final String path, long fadeIn, long stay, long fadeOut) {
        if (Tnttag.customizationfile.getString(path).isEmpty()) return;

        player.sendTitle(colorize(Tnttag.customizationfile.getString(path + ".title")), colorize(Tnttag.customizationfile.getString(path + ".subtitle")), (int) fadeIn, (int) stay, (int) fadeOut);
    }

    public static void sendTitle(final Player player, final String path, long fadeIn, long stay, long fadeOut, int seconds) {
        if (Tnttag.customizationfile.getString(path).isEmpty()) return;

        player.sendTitle(colorize(Tnttag.customizationfile.getString(path + ".title")).replace("{seconds}", String.valueOf(seconds)), colorize(Tnttag.customizationfile.getString(path + ".subtitle").replace("{seconds}", String.valueOf(seconds))), (int) fadeIn, (int) stay, (int) fadeOut);
    }

    public static void sendActionBarMessage(final Player player, final String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colorize(message)));
    }

    public static void sendCustomMessage(final Player player, final String msg) {
        if (msg == null) return;
        String message = msg.replace("{player}", player.getName());
        player.sendMessage(colorize(message));
    }

    public static String getRaw(String path) {
        return Tnttag.customizationfile.getString(path);
    }
}
