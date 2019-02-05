package nl.tabuu.hopperx.task;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.XHopper;
import org.bukkit.Material;

public class HopperTask implements Runnable{

    private XHopper _hopper;
    private long _timestamp;

    public HopperTask(XHopper hopper){
        _hopper = hopper;
        reset();
    }

    public void reset(){
        _timestamp = System.currentTimeMillis();
    }

    @Override
    public void run() {
        if(getHopper().getLocation().getBlock().getType().equals(Material.HOPPER)){
            int x = getHopper().getLocation().getChunk().getX();
            int z = getHopper().getLocation().getChunk().getZ();

            _hopper.setActive(_hopper.getLocation().getWorld().isChunkInUse(x, z));

            if(_hopper.isActive() && !_hopper.getLocation().getBlock().isBlockIndirectlyPowered()){
                _hopper.update((System.currentTimeMillis() - _timestamp) / 1000d);
            }
            _hopper.getLocation().getBlock().getState().update();

            // _hopper.getHopper().update();
        }
        else
            HopperX.getInstance().getHopperManager().unregister(getHopper());
    }

    public XHopper getHopper(){
        return _hopper;
    }
}
