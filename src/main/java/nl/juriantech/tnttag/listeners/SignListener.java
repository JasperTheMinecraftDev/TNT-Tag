package nl.juriantech.tnttag.listeners;

import nl.juriantech.tnttag.Arena;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.managers.SignManager;
import nl.juriantech.tnttag.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {

    private final Tnttag plugin;
    private final SignManager signManager;

    public SignListener(Tnttag plugin) {
        this.plugin = plugin;
        this.signManager = plugin.getSignManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Player player = event.getPlayer();
                Block block = event.getClickedBlock();
                Sign sign = (Sign) block.getState();
                if (signManager.isTNTTagSign(block.getLocation())) {
                    String targetMap = ChatColor.stripColor(sign.getLine(1));

                    player.getInventory().setHeldItemSlot(0);
                    player.performCommand("tnttag join " + targetMap);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (signManager.isTNTTagSign(block.getLocation())) {
                if (!event.getPlayer().hasPermission("tnttag.breaksigns")) {
                    ChatUtils.sendMessage(event.getPlayer(), "general.no-permission");
                    event.setCancelled(true);
                } else {
                    if (plugin.getArenaManager().getArena(ChatColor.stripColor(sign.getLine(1))) != null) {
                        signManager.removeSign(ChatColor.stripColor(sign.getLine(1)), block.getLocation());
                    }
                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        if (lines[0].equals("[TT]")) {
            if (!player.hasPermission("tnttag.createsigns")) {
                ChatUtils.sendMessage(player, "general.no-permission");
                return;
            }

            String targetMap = lines[1];
            Arena arena = plugin.getArenaManager().getArena(targetMap);
            if (arena == null) return;

            event.setLine(0, "Hold on..");
            event.setLine(1, "Waiting for the");
            event.setLine(2, "sign update.");

            Location location = event.getBlock().getLocation();
            signManager.addSign(arena.getName(), location);
        }
    }
}