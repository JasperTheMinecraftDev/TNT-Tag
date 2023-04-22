package nl.juriantech.tnttag.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ArenaStartedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String arenaName;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ArenaStartedEvent(String arenaName) {
        this.arenaName = arenaName;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getArenaName() {
        return arenaName;
    }
}
