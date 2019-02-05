package nl.tabuu.hopperx.database;

import nl.tabuu.hopperx.hopper.XHopper;
import org.bukkit.Location;

import java.util.HashMap;

public interface IHopperDatabase {
    HashMap<Location, XHopper> load();
    void save(HashMap<Location, XHopper> data);
}
