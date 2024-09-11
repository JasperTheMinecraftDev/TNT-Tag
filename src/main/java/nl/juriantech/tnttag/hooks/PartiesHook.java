package nl.juriantech.tnttag.hooks;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class PartiesHook {

    public Party getPlayerParty(UUID playerUUID) {
        PartiesAPI api = Parties.getApi();
        String partyDescription = null;
        PartyPlayer player = api.getPartyPlayer(playerUUID);
        if (player.isInParty()) {
            return api.getParty(player.getPartyId());
        } else {
            return null;
        }
    }

    public ArrayList<Player> getPlayersOfParty(Party party) {
        ArrayList<Player> players = new ArrayList<>();
        for (PartyPlayer player : party.getOnlineMembers()) {
            players.add(Bukkit.getPlayer(player.getPlayerUUID()));
        }
        return players;
    }
}
