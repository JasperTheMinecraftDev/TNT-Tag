package nl.juriantech.tnttag.signs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface SignInterface {
    void onClick(Player player);
    void update();
    Location getLoc();
}
