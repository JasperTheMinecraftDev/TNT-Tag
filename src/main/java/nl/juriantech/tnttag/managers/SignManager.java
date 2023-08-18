package nl.juriantech.tnttag.managers;

import dev.dejvokep.boostedyaml.YamlDocument;
import nl.juriantech.tnttag.Tnttag;
import nl.juriantech.tnttag.enums.SignType;
import nl.juriantech.tnttag.signs.JoinSign;
import nl.juriantech.tnttag.signs.LeaveSign;
import nl.juriantech.tnttag.signs.SignInterface;
import nl.juriantech.tnttag.signs.TopSign;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.*;

public class SignManager {

    private final Tnttag plugin;
    private final ArrayList<JoinSign> joinSigns;
    private final ArrayList<LeaveSign> leaveSigns;
    private final ArrayList<TopSign> topSigns;
    public static final String SIGN_PREFIX = ChatColor.GOLD + "[" + ChatColor.RED + "Tnttag" + ChatColor.GOLD + "]";
    public SignManager(Tnttag plugin) {
        this.plugin = plugin;
        this.joinSigns = new ArrayList<>();
        this.leaveSigns = new ArrayList<>();
        this.topSigns = new ArrayList<>();
    }

    public void addJoinSign(JoinSign sign) {
        joinSigns.add(sign);

        //The sign is not yet placed so we have to wait a second.
        new BukkitRunnable() {
            @Override
            public void run() {
                sign.update();
            }
        }.runTaskLater(plugin, 20);
        saveSigns();
    }

    public void addLeaveSign(LeaveSign sign) {
        leaveSigns.add(sign);
        //The sign is not yet placed so we have to wait a second.
        new BukkitRunnable() {
            @Override
            public void run() {
                sign.update();
            }
        }.runTaskLater(plugin, 20);
        saveSigns();
    }

    public void addTopSign(TopSign sign) {
        topSigns.add(sign);
        //The sign is not yet placed so we have to wait a second.
        new BukkitRunnable() {
            @Override
            public void run() {
                sign.update();
            }
        }.runTaskLater(plugin, 20);
        saveSigns();
    }

    public SignType getSignType(Sign sign) {
        Location signLocation = sign.getLocation();

        if (joinSigns.stream().anyMatch(joinSign -> joinSign.getLoc().equals(signLocation))) {
            return SignType.JOIN;
        } else if (leaveSigns.stream().anyMatch(leaveSign -> leaveSign.getLoc().equals(signLocation))) {
            return SignType.LEAVE;
        } else if (topSigns.stream().anyMatch(topSign -> topSign.getLoc().equals(signLocation))) {
            return SignType.TOP;
        }

        return null;
    }

    public boolean isPluginSign(Sign sign) {
        return getSignType(sign) != null;
    }

    public SignInterface getPluginSign(Sign sign) {
        SignType type = getSignType(sign);
        if (type == null) {
            return null;
        }

        Location signLocation = sign.getLocation();

        switch (type) {
            case JOIN:
                return joinSigns.stream()
                        .filter(joinSign -> joinSign.getLoc().equals(signLocation))
                        .findFirst()
                        .orElse(null);
            case LEAVE:
                return leaveSigns.stream()
                        .filter(leaveSign -> leaveSign.getLoc().equals(signLocation))
                        .findFirst()
                        .orElse(null);
            case TOP:
                return topSigns.stream()
                        .filter(topSign -> topSign.getLoc().equals(signLocation))
                        .findFirst()
                        .orElse(null);
            default:
                throw new ArithmeticException("Invalid SignType enum.");
        }
    }

    public void removeSign(Sign sign) {
        SignType type = getSignType(sign);
        if (type == null) return;

        switch(type) {
            case JOIN:
                JoinSign joinSign = joinSigns.stream()
                        .filter(joinSign2 -> joinSign2.getLoc().equals(sign.getLocation()))
                        .findAny()
                        .orElse(null);
                if (joinSign == null) return;

                joinSigns.remove(joinSign);
                saveSigns();
                break;
            case LEAVE:
                LeaveSign leaveSign = leaveSigns.stream()
                        .filter(leaveSign2 -> leaveSign2.getLoc().equals(sign.getLocation()))
                        .findAny()
                        .orElse(null);
                if (leaveSign == null) return;

                leaveSigns.remove(leaveSign);
                saveSigns();
                break;
            case TOP:
                TopSign topSign = topSigns.stream()
                        .filter(topSign2 -> topSign2.getLoc().equals(sign.getLocation()))
                        .findAny()
                        .orElse(null);
                if (topSign == null) return;

                topSigns.remove(topSign);
                saveSigns();
                break;
            default:
                throw new ArithmeticException("Invalid SignType enum.");
        }
    }

    public void updateSigns() {
        joinSigns.forEach(JoinSign::update);
        leaveSigns.forEach(LeaveSign::update);
        topSigns.forEach(TopSign::update);
    }

    public void saveSigns() {
        YamlDocument signsDataFile = Tnttag.signsdatafile;
        signsDataFile.clear(); // We do not want duplicate entries.

        ArrayList<String> joinSigns = new ArrayList<>();
        ArrayList<String> leaveSigns = new ArrayList<>();
        ArrayList<String> topSigns = new ArrayList<>();

        this.joinSigns.forEach(joinSign -> joinSigns.add(joinSign.toString()));
        this.leaveSigns.forEach(leaveSign -> leaveSigns.add(leaveSign.toString()));
        this.topSigns.forEach(topSign -> topSigns.add(topSign.toString()));

        signsDataFile.set("joinSigns", joinSigns);
        signsDataFile.set("leaveSigns", leaveSigns);
        signsDataFile.set("topSigns", topSigns);

        try {
            signsDataFile.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadSigns() {
        YamlDocument signsDataFile = Tnttag.signsdatafile;
        for (String str : signsDataFile.getStringList("joinSigns")) {
            joinSigns.add(JoinSign.fromString(plugin, str));
        }


        for (String str : signsDataFile.getStringList("leaveSigns")) {
            leaveSigns.add(LeaveSign.fromString(plugin, str));
        }

        for (String str : signsDataFile.getStringList("topSigns")) {
            topSigns.add(TopSign.fromString(plugin, str));
        }
    }
}