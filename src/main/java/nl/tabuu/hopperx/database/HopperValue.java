package nl.tabuu.hopperx.database;

import nl.tabuu.hopperx.HopperX;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Optional;

public enum HopperValue {

    FILTER_WHITELIST(Material.class),
    FILTER_VOIDLIST(Material.class),
    SUCTION_RANGE(Double.class),
    SPEED(Integer.class),
    MAX_LINK_DISTANCE(Integer.class),
    BLOCK_BREAKING(Boolean.class),
    TELEPORTING(Boolean.class),
    SUCKING(Boolean.class),
    FILTER(Boolean.class),
    FARMING(Boolean.class),
    DESTINATION_LINKED(Location.class),
    DESTINATION_BLACKLIST(Location.class);

    private static final String PREFIX = "DefaultValues.";

    Class _class;
    HopperValue(Class clazz){
        _class = clazz;
    }

    public Optional getDefault(){
        Object value = HopperX.getInstance().getConfigurationManager().getConfiguration("config").get(PREFIX + name(), _class);
        if(value == null)
            return Optional.empty();
        return Optional.of(value);
    }

}
