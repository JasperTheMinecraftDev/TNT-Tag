package nl.juriantech.tnttag.hooks;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PartyAndFriendsHook {

    public PlayerParty getPlayerParty(UUID playerUUID) {
        PAFPlayer pafPlayer = PAFPlayerManager.getInstance().getPlayer(playerUUID);
        return PartyManager.getInstance().getParty(pafPlayer);
    }

    public boolean playerIsInParty(UUID playerUUID) {
        PlayerParty party = getPlayerParty(playerUUID);

        return party != null;
    }

    public ArrayList<Player> getPlayersOfParty(PlayerParty party) {
        ArrayList<Player> players = new ArrayList<>();
        for (PAFPlayer player : party.getPlayers()) {
            players.add(Bukkit.getPlayer(player.getUniqueId()));
        }
        return players;
    }
}
