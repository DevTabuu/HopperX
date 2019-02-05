package nl.tabuu.hopperx.database;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.XHopper;
import nl.tabuu.hopperx.hopper.XHopperDestination;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.serialization.string.Serializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class HopperConfig implements IHopperDatabase {
    private IConfiguration _data;

    public HopperConfig(IConfiguration data){
        _data = data;
    }

    public HopperConfig(){
        this(
                HopperX.getInstance().getConfigurationManager().getConfiguration("hopper-data")
        );
    }

    @Override
    public HashMap<Location, XHopper> load() {
        HashMap<Location, XHopper> loadedHoppers = new HashMap<>();

        if(_data.getConfigurationSection("Hoppers") == null)
            return loadedHoppers;

        for(String ls : _data.getConfigurationSection("Hoppers").getKeys(false)){
            Location location = Serializer.LOCATION.deserialize(ls);

            if(location == null)
                continue;

            String prefix = "Hoppers." + ls + ".";
            if(!location.getBlock().getType().equals(Material.HOPPER))
                continue;

            Hopper hopperBlock = (Hopper) location.getBlock().getState();
            Map<XHopperDestination, Optional<Inventory>> destinations = new LinkedHashMap<>();
            for(XHopperDestination hopperDestination : XHopperDestination.values()){
                Location destinationLocation = _data.getLocation(prefix + "DESTINATION_" + hopperDestination.name());
                if(destinationLocation == null || !Container.class.isInstance(destinationLocation.getBlock().getState()))
                    destinations.put(hopperDestination, Optional.empty());
                else
                    destinations.put(hopperDestination, Optional.of(((Container) destinationLocation.getBlock().getState()).getInventory()));
            }

            // Default values?
            XHopper hopper = new XHopper(
                    hopperBlock,
                    destinations,
                    _data.getEnumList(Material.class, prefix + HopperValue.FILTER_WHITELIST.name()),
                    _data.getEnumList(Material.class, prefix + HopperValue.FILTER_VOIDLIST.name()),
                    _data.getDouble(prefix + HopperValue.SUCTION_RANGE.name()),
                    _data.getInt(prefix + HopperValue.SPEED.name()),
                    _data.getBoolean(prefix + HopperValue.BLOCK_BREAKING.name()),
                    _data.getBoolean(prefix + HopperValue.TELEPORTING.name()),
                    _data.getBoolean(prefix + HopperValue.SUCKING.name()),
                    _data.getBoolean(prefix + HopperValue.FILTER.name()),
                    _data.getBoolean(prefix + HopperValue.FARMING.name())
                    );
            loadedHoppers.put(location, hopper);
        }

        return loadedHoppers;
    }

    @Override
    public void save(HashMap<Location, XHopper> data) {
        _data.delete("Hoppers");

        for(Map.Entry<Location, XHopper> entry : data.entrySet()){
            Location location = entry.getKey();
            XHopper hopper = entry.getValue();
            String prefix = "Hoppers." + Serializer.LOCATION.serialize(location)+ ".";

            if(!location.getBlock().getType().equals(Material.HOPPER))
                continue;

            for(HopperValue hopperValue : HopperValue.values())
                _data.set(prefix + hopperValue.name(), hopper.getValueByHopperValue(hopperValue));
        }
        _data.save();
    }
}
