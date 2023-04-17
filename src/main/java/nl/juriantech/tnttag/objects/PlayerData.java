package nl.juriantech.tnttag.objects;

import nl.juriantech.tnttag.Tnttag;

import java.io.IOException;
import java.util.*;

public class PlayerData {

    private final UUID uuid;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void setWins(int wins) {
        Tnttag.playerdatafile.set(uuid + ".wins", wins);
        savePlayerDataFile();
    }

    public Integer getWins() {
        if (Tnttag.playerdatafile.getInt(uuid + ".wins") == null) {
            return 0;
        }
        return Tnttag.playerdatafile.getInt(uuid + ".wins");
    }

    public void setTimesTagged(int timestagged) {
        Tnttag.playerdatafile.set(uuid + ".timestagged", timestagged);
        savePlayerDataFile();
    }

    public Integer getTimesTagged() {
        if (Tnttag.playerdatafile.getInt(uuid + ".timestagged") == null) {
            return 0;
        }
        return Tnttag.playerdatafile.getInt(uuid + ".timestagged");
    }

    public void setTags(int tags) {
        Tnttag.playerdatafile.set(uuid + ".tags", tags);
        savePlayerDataFile();
    }

    public Integer getTags() {
        if (Tnttag.playerdatafile.getInt(uuid + ".tags") == null) {
            return 0;
        }
        return Tnttag.playerdatafile.getInt(uuid + ".tags");
    }

    public TreeMap<UUID, Integer> getWinsData() {
        TreeMap<UUID, Integer> winsData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".wins") != null) {
                int wins = Tnttag.playerdatafile.getInt(route + ".wins");
                winsData.put(UUID.fromString(route), wins);
            }
        }
        return winsData;
    }

    public TreeMap<UUID, Integer> getTimesTaggedData() {
        TreeMap<UUID, Integer> timesTaggedData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".timestagged") != null) {
                int kills = Tnttag.playerdatafile.getInt(route + ".timestagged");
                timesTaggedData.put(UUID.fromString(route), kills);
            }
        }
        return timesTaggedData;
    }

    public TreeMap<UUID, Integer> getTagsData() {
        TreeMap<UUID, Integer> tagsData = new TreeMap<>();
        for (String route : Tnttag.playerdatafile.getRoutesAsStrings(false)) {
            if (Tnttag.playerdatafile.getInt(route + ".tags") != null) {
                int deaths = Tnttag.playerdatafile.getInt(route + ".tags");
                tagsData.put(UUID.fromString(route), deaths);
            }
        }
        return tagsData;
    }

    private void savePlayerDataFile() {
        try {
            Tnttag.playerdatafile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
