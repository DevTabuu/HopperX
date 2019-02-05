package nl.tabuu.hopperx.utils;

import nl.tabuu.hopperx.hopper.XHopperDestination;
import nl.tabuu.tabuucore.item.ItemList;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class Methods {
    /**
     * This function adds items to an inventory the way a hopper would.
     * @param targets   A HashMap containing Inventories and their representative XHopperDestination.
     * @param items     An ItemList containing all items that should be added to the inventories.
     * @return          An ItemList containing all items that have been successfully added to the inventories.
     */
    public static ItemList hopperTransfer(Map<XHopperDestination, Optional<Inventory>> targets, ItemList items, int count){
        ItemList transferredItems = new ItemList();

        for(Map.Entry<XHopperDestination, Optional<Inventory>> entry : targets.entrySet()){
            if(items.isEmpty())
                break;

            else if(!entry.getValue().isPresent())
                continue;

            Inventory inventory = entry.getValue().get();
            XHopperDestination destination = entry.getKey();
            InventoryTypeInfo info = InventoryTypeInfo.getTypeByInventory(inventory);
            int[] entrySlots = info.getEntrySlots(destination);

            if(entrySlots == null){
                entrySlots = new int[inventory.getSize()];
                for(int i = 0; i < entrySlots.length; i++)
                    entrySlots[i] = i;
            }

            for(int slot : entrySlots){
                if(items.isEmpty())
                    break;

                for(int i = 0; i < items.size(); i++){
                    ItemStack slotItem = inventory.getItem(slot);
                    ItemStack moveItem = items.get(i);

                    if(info.hasWhitelist(slot) && !Arrays.asList(info.getWhitelist(slot)).contains(moveItem.getType()))
                        continue;

                    if(moveItem == null || moveItem.getType().equals(Material.AIR))
                        continue;

                    int toMove;
                    if(slotItem == null)
                        toMove = moveItem.getAmount();
                    else if(slotItem.isSimilar(moveItem) && (slotItem.getAmount() < slotItem.getMaxStackSize()))
                        toMove = Math.min(slotItem.getMaxStackSize() - slotItem.getAmount(), moveItem.getAmount());
                    else
                        continue;

                    toMove = Math.min(toMove, count - transferredItems.itemCount());

                    if(toMove > 0){
                        ItemStack transferredItem = moveItem.clone();
                        transferredItem.setAmount(toMove);
                        transferredItems.add(transferredItem);
                        moveItem.setAmount(moveItem.getAmount() - toMove);

                        if(slotItem != null)
                            slotItem.setAmount(slotItem.getAmount() + toMove);
                        else
                            inventory.setItem(slot, transferredItem);
                    }
                }
            }
        }
        transferredItems.squash();
        return transferredItems;
    }
}
