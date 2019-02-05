package nl.tabuu.hopperx.farming;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class BlockStack {

    List<Block> _blocks;

    public BlockStack(Block start, BlockFace blockFace, boolean bidirectional){
        _blocks = new ArrayList<>();
        Block nextBlock = start;
        while (nextBlock.getType().equals(start.getType())){
            _blocks.add(nextBlock);
            nextBlock = nextBlock.getRelative(blockFace);
        }
        if(bidirectional){
            BlockFace oppositeFace = blockFace.getOppositeFace();
            nextBlock = start.getRelative(oppositeFace);
            while (nextBlock.getType().equals(start.getType())){
                _blocks.add(nextBlock);
                nextBlock = nextBlock.getRelative(oppositeFace);
            }
        }
    }

    public BlockStack(Block base){
        this(base, BlockFace.UP, false);
    }

    public List<Block> getBlocks(){
        return _blocks;
    }

    public Block getBase(){
        return _blocks.get(0);
    }

    public int getStackSize(){
        return _blocks.size();
    }
}
