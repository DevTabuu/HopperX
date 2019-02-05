package nl.tabuu.hopperx.hopper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HopperLinkWrapper {

    private UUID _linkingPlayer;
    private long _timestamp;
    private XHopper _hopper;
    private XHopperDestination _hopperDestination;

    public HopperLinkWrapper(UUID linkingPlayer, long timestamp, XHopper hopper, XHopperDestination hopperDestination) {
        _linkingPlayer      = linkingPlayer;
        _timestamp          = timestamp;
        _hopper             = hopper;
        _hopperDestination  = hopperDestination;
    }

    public Player getPlayer(){
        return Bukkit.getPlayer(_linkingPlayer);
    }

    public long getTimestamp(){
        return _timestamp;
    }

    public XHopper getHopper(){
        return _hopper;
    }

    public XHopperDestination getHopperDestination(){
        return _hopperDestination;
    }
}
