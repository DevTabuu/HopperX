package nl.tabuu.hopperx.farming;

import nl.tabuu.tabuucore.item.ItemList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class FarmableBlockStack extends BlockStack{

    public FarmableBlockStack(Block base){
        super(base);
    }

    public ItemList farm(ItemStack tool, boolean dropItems){
        ItemList drops = new ItemList();

        for(Block block : getBlocks()) {
            if(block.equals(getBase()))
                continue;
            drops.addAll(block.getDrops(tool));
            if(dropItems)
                block.breakNaturally(tool);
            else
                block.setType(Material.AIR);
        }

        return drops;
    }

}
