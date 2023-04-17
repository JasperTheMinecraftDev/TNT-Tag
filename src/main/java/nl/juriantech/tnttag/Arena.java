package nl.juriantech.tnttag;

import nl.juriantech.tnttag.managers.GameManager;
import org.bukkit.Location;

import java.util.ArrayList;

public class Arena {

    private final String name;
    private int maxPlayers;
    private int minPlayers;
    private Location lobbyLocation;
    private Location startLocation;
    private final ArrayList<String> potionEffects;
    private int roundDuration;
    private int countdown;
    private final GameManager gameManager;


    public Arena(Tnttag plugin, String name, Location startLocation, Location lobbyLocation, int maxPlayers, int minPlayers, ArrayList<String> potionEffects, int roundDuration, int countdown) {
        this.name = name;
        this.startLocation = startLocation;
        this.lobbyLocation = lobbyLocation;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.potionEffects = potionEffects;
        this.roundDuration = roundDuration;
        this.countdown = countdown;
        this.gameManager = new GameManager(plugin, this);
    }

    public String getName() {
        return name;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }


    public Location getStartLocation() {
        return startLocation;
    }

    public ArrayList<String> getPotionEffects() {
        return potionEffects;
    }

    public int getRoundDuration() {
        return roundDuration;
    }

    public int getCountdown() {
        return countdown;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void setRoundDuration(int roundDuration) {
        this.roundDuration = roundDuration;
    }
}