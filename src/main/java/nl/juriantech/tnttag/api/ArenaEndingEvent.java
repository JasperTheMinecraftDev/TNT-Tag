package nl.juriantech.tnttag.api;

import nl.juriantech.tnttag.enums.PlayerType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class ArenaEndingEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String arenaName;
    private HashMap<Player, PlayerType> players;
    private ArrayList<Player> winners;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ArenaEndingEvent(String arenaName, HashMap<Player, PlayerType> players, ArrayList<Player> winners) {
        this.arenaName = arenaName;
        this.players = players;
        this.winners = winners;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public String getArenaName() {
        return arenaName;
    }

    public HashMap<Player, PlayerType> getPlayers() {
        return players;
    }

    public ArrayList<Player> getWinners() {
        return winners;
    }
}
