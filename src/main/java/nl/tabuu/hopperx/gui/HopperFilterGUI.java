package nl.tabuu.hopperx.gui;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.XHopper;
import nl.tabuu.tabuucore.inventory.InventorySize;
import nl.tabuu.tabuucore.inventory.ui.InventoryUI;
import nl.tabuu.tabuucore.inventory.ui.InventoryUIClick;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.Brush;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.IBrush;
import nl.tabuu.tabuucore.item.ItemBuilder;
import nl.tabuu.tabuucore.util.Dictionary;
import nl.tabuu.tabuucore.util.vector.Vector2f;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class HopperFilterGUI extends InventoryUI {

    XHopper _hopper;
    Dictionary _local;

    public HopperFilterGUI(XHopper hopper){
        super("Hopper Filter", InventorySize.FOUR_ROWS);
        _hopper = hopper;
        _local = HopperX.getInstance().getConfigurationManager().getConfiguration("lang").getDictionary("");
        setTile(_local.translate("FILTER_GUI_TITLE"));
        reload();
    }

    @Override
    public void onClick(Player player, InventoryUIClick click){
        Sound clickSound = Sound.BLOCK_STONE_BUTTON_CLICK_ON;
        float enablePitch = 0.2f;
        float disablePitch = 1;

        List<Integer> whitelistSlots = Arrays.asList(10, 11, 12, 19, 20, 21);
        List<Integer> voidlistSlots = Arrays.asList(14, 15, 16, 23, 24, 25);

        List<Material> whitelist = _hopper.getWhitelist();
        List<Material> voidlist = _hopper.getVoidFilter();

        if(click.isLeftClick()){
            switch (click.getSlot()){
                case 31:
                    player.playSound(player.getLocation(), clickSound, 1, enablePitch);
                    player.openInventory(new HopperGUI(_hopper).getInventory());
                    break;

                default:
                    if(click.getCursorItem().getType().equals(Material.AIR) || !click.getClickedItem().getType().equals(Material.AIR))
                        break;
                    if(whitelistSlots.contains(click.getSlot())){
                        whitelist.add(click.getCursorItem().getType());
                    }
                    else if(voidlistSlots.contains(click.getSlot())){
                        voidlist.add(click.getCursorItem().getType());
                    }
                    break;
            }
        }
        else if(click.isRightClick()){
            switch (click.getSlot()){
                default:
                    if(!click.getCursorItem().getType().equals(Material.AIR) || click.getClickedItem().getType().equals(Material.AIR))
                        break;
                    if(whitelistSlots.contains(click.getSlot())){
                        whitelist.remove(click.getClickedItem().getType());
                    }
                    else if(voidlistSlots.contains(click.getSlot())){
                        voidlist.remove(click.getClickedItem().getType());
                    }
                    break;
            }
        }
        _hopper.setVoidFilter(voidlist);
        _hopper.setWhitelist(whitelist);
        reload();
    }

    @Override
    protected void draw() {
        IBrush brush = new Brush(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build());
        setBrush(brush);

        drawFilledRectangle(new Vector2f(0, 0), new Vector2f(8, 4));

        ItemBuilder back = new ItemBuilder(Material.BARRIER);
        back.setDisplayName(_local.translate("FILTER_GUI_BACK"));

        ItemBuilder whiteList = new ItemBuilder(Material.WHITE_WOOL);
        whiteList.setDisplayName(_local.translate("FILTER_GUI_WHITELIST"));
        whiteList.setLore(_local.translate("FILTER_GUI_WHITELIST_DESC"));

        ItemBuilder voidList = new ItemBuilder(Material.BLACK_WOOL);
        voidList.setDisplayName(_local.translate("FILTER_GUI_VOID"));
        voidList.setLore(_local.translate("FILTER_GUI_VOID_DESC"));

        List<Material> whiteMaterials = _hopper.getWhitelist();
        List<Material> voidMaterials = _hopper.getVoidFilter();

        int i = 0;
        for(int x = 1; x < 4; x++){
            for(int y = 1; y < 3; y++){
                if(whiteMaterials.size() <= i) break;
                setItemAt(new Vector2f(x, y), new ItemStack(whiteMaterials.get(i), 1));
                i++;
            }
        }

        i = 0;
        for(int x = 5; x < 8; x++){
            for(int y = 1; y < 3; y++){
                if(voidMaterials.size() <= i) break;
                setItemAt(new Vector2f(x, y), new ItemStack(voidMaterials.get(i), 1));
                i++;
            }
        }

        setItemAt(slotToVector(31), back.build());
        setItemAt(slotToVector(2), whiteList.build());
        setItemAt(slotToVector(6), voidList.build());
    }
}
