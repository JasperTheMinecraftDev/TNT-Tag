package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.SignType;
import nl.juriantech.tnttag.enums.StatType;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.signs.JoinSign;
import nl.juriantech.tnttag.signs.LeaveSign;
import nl.juriantech.tnttag.signs.SignInterface;
import nl.juriantech.tnttag.signs.TopSign;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class SignListener implements Listener {

    private final Tnttag plugin;
    private final SignManager signManager;
    private static final String HEADER = "[TT]";

    public SignListener(Tnttag plugin) {
        this.plugin = plugin;
        this.signManager = plugin.getSignManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            Sign sign = (Sign) block.getState();

            //If the player tries to break the sign.
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (signManager.isPluginSign(sign)) {
                    if (!event.getPlayer().hasPermission("tnttag.breaksigns")) {
                        ChatUtils.sendMessage(event.getPlayer(), "general.no-permission");
                        event.setCancelled(true);
                    } else {
                        if (plugin.getArenaManager().getArena(ChatColor.stripColor(sign.getLine(1))) != null) {
                            signManager.removeSign(sign);
                            block.setType(Material.AIR);
                        }

                        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
                        }
                    }
                }
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (signManager.isPluginSign(sign)) {
                    SignInterface sign1 = signManager.getPluginSign(sign);
                    sign1.onClick(player);
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();
        if (!lines[0].equals(HEADER)) return;
        if (Arrays.stream(SignType.values()).filter(signType -> signType.toString().equals(lines[1].toUpperCase()))
                .findAny()
                .orElse(null) == null) return;

        if (!player.hasPermission("tnttag.createsigns")) {
            ChatUtils.sendMessage(player, "general.no-permission");
            return;
        }

        SignType signType = SignType.valueOf(lines[1].toUpperCase());

        switch(signType) {
            case JOIN:
                if (lines[2] == null) return;
                if (plugin.getArenaManager().getArena(lines[2]) == null) return;

                signManager.addJoinSign(new JoinSign(plugin, lines[2], event.getBlock().getLocation()));
                break;
            case LEAVE:
                signManager.addLeaveSign(new LeaveSign(event.getBlock().getLocation()));
                break;
            case TOP:
                if (Arrays.stream(StatType.values()).filter(statType -> statType.toString().equals(lines[2].toUpperCase()))
                        .findAny()
                        .orElse(null) == null) return;

                try {
                    Integer.parseInt(lines[3]);
                } catch (NumberFormatException e) {
                    return;
                }

                signManager.addTopSign(new TopSign(event.getBlock().getLocation(), Integer.parseInt(lines[3]), StatType.valueOf(lines[2].toUpperCase())));
        }
    }
}