package nl.tabuu.hopperx.listener;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.XHopper;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class HopperListeners implements Listener{

    @EventHandler
    public void onHopperHop(InventoryMoveItemEvent e){
        InventoryHolder holderFrom = e.getInitiator().getHolder();
        if(e.getSource().getHolder() instanceof Minecart || e.getDestination().getHolder() instanceof Minecart)
            return;
        if(holderFrom != null && (holderFrom instanceof Hopper || holderFrom.getInventory().getType().equals(InventoryType.HOPPER))) {
            HopperX.getInstance().getHopperManager().register((Hopper) holderFrom);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHopperSuck(InventoryPickupItemEvent e){

        if(e.getInventory().getHolder() instanceof Hopper){
            HopperX.getInstance().getHopperManager().register((Hopper) e.getInventory().getHolder());

            XHopper hopper = HopperX.getInstance().getHopperManager().getHopperAt(e.getInventory().getLocation());

            if(!hopper.isWhitelisted(e.getItem().getItemStack()))
                e.setCancelled(true);
        }
    }

}
