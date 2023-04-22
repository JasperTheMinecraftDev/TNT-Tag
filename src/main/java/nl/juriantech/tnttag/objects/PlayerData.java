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

    private void savePlayerDataFile() {
        try {
            Tnttag.playerdatafile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
