package nl.juriantech.tnttag.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLeaveArenaEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String arenaName;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PlayerLeaveArenaEvent(Player player, String arenaName) {
        this.player = player;
        this.arenaName = arenaName;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public String getArenaName() {
        return arenaName;
    }
}