package nl.tabuu.hopperx.gui;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.hopper.XHopper;
import nl.tabuu.hopperx.hopper.XHopperDestination;
import nl.tabuu.tabuucore.inventory.InventorySize;
import nl.tabuu.tabuucore.inventory.ui.InventoryUI;
import nl.tabuu.tabuucore.inventory.ui.InventoryUIClick;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.Brush;
import nl.tabuu.tabuucore.inventory.ui.graphics.brush.IBrush;
import nl.tabuu.tabuucore.item.ItemBuilder;
import nl.tabuu.tabuucore.serialization.string.Serializer;
import nl.tabuu.tabuucore.util.Dictionary;
import nl.tabuu.tabuucore.util.vector.Vector2f;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HopperGUI extends InventoryUI {

    private XHopper _hopper;
    private Dictionary _local;

    public HopperGUI(XHopper hopper){
        super("Hopper", InventorySize.FOUR_ROWS);

        _local = HopperX.getInstance().getConfigurationManager().getConfiguration("lang").getDictionary("");
        _hopper = hopper;

        setTile(_local.translate("GUI_TITLE"));
        reload();
    }

    @Override
    public void onClick(Player player, InventoryUIClick click){
        Sound clickSound = Sound.BLOCK_STONE_BUTTON_CLICK_ON;
        float enablePitch = 0.2f;
        float disablePitch = 1;

        if(click.isLeftClick()){
            switch (click.getSlot()){
                case 9:
                    player.playSound(player.getLocation(), clickSound, 1, _hopper.isBlockBreaking() ? enablePitch : disablePitch);
                    _hopper.setBlockBreaking(!_hopper.isBlockBreaking());
                    break;
                case 11:
                    player.playSound(player.getLocation(), clickSound, 1, _hopper.isSucking() ? enablePitch : disablePitch);
                    _hopper.setSucking(!_hopper.isSucking());
                    break;
                case 13:
                    player.playSound(player.getLocation(), clickSound, 1, _hopper.isFilter() ? enablePitch : disablePitch);
                    _hopper.setFilter(!_hopper.isFilter());
                    break;
                case 15:
                    player.playSound(player.getLocation(), clickSound, 1, _hopper.isTeleporting() ? enablePitch : disablePitch);
                    _hopper.setTeleporting(!_hopper.isTeleporting());
                    break;
                case 17:
                    player.playSound(player.getLocation(), clickSound, 1, _hopper.isFarming() ? enablePitch : disablePitch);
                    _hopper.setFarming(!_hopper.isFarming());
                    break;
                case 31:
                    HopperX.getInstance().getHopperManager().startLinking(player, _hopper, XHopperDestination.LINKED);
                    player.playSound(player.getLocation(), clickSound, 1, disablePitch);
                    player.closeInventory();
                    break;
            }
        }
        else if(click.isRightClick()){
            switch (click.getSlot()){
                case 13:
                    player.playSound(player.getLocation(), clickSound, 1, enablePitch);
                    player.openInventory(new HopperFilterGUI(_hopper).getInventory());

                    break;

                case 31:
                    player.playSound(player.getLocation(), clickSound, 1, enablePitch);
                    _hopper.setDestination(XHopperDestination.LINKED, null);
                    break;
            }
        }


        reload();
    }

    @Override
    protected void draw() {
        IBrush brush = new Brush(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build());
        setBrush(brush);

        drawRectangle(new Vector2f(0, 0), new Vector2f(8, 4));

        ItemStack enabled = new ItemStack(Material.LIME_WOOL, 1);
        ItemStack disabled = new ItemStack(Material.RED_WOOL, 1);

        ItemBuilder breaking = new ItemBuilder(_hopper.isBlockBreaking() ? enabled : disabled);
        breaking.setDisplayName(_local.translate("GUI_BLOCKBREAK_TITLE"));
        breaking.setLore(_local.translate("GUI_BLOCKBREAK_DESC"));

        ItemBuilder suction = new ItemBuilder(_hopper.isSucking() ? enabled : disabled);
        suction.setDisplayName(_local.translate("GUI_SUCTION_TITLE"));
        suction.setLore(_local.translate("GUI_SUCTION_DESC", "{RADIUS}", _hopper.getSuctionRange() + ""));

        ItemBuilder itemFilter = new ItemBuilder(_hopper.isFilter() ? enabled : disabled);
        itemFilter.setDisplayName(_local.translate("GUI_FILTER_TITLE"));
        itemFilter.setLore(_local.translate("GUI_FILTER_DESC"));

        ItemBuilder teleport = new ItemBuilder(_hopper.isTeleporting() ? enabled : disabled);
        teleport.setDisplayName(_local.translate("GUI_TELEPORTATION_TITLE"));
        teleport.setLore(_local.translate("GUI_TELEPORTATION_DESC"));

        ItemBuilder farming = new ItemBuilder(_hopper.isFarming() ? enabled : disabled);
        farming.setDisplayName(_local.translate("GUI_FARMING_TITLE"));
        farming.setLore(_local.translate("GUI_FARMING_DESC"));

        ItemBuilder link = new ItemBuilder(Material.TRIPWIRE_HOOK);
        link.setDisplayName(_local.translate("GUI_LINK_TITLE"));
        if(_hopper.getDestination(XHopperDestination.LINKED).isPresent())
            link.setLore(_local.translate("GUI_LINK_DESC",
                    "{CONTAINER_TYPE}", formatEnum(_hopper.getDestination(XHopperDestination.LINKED).get().getType()),
                    "{LOCATION}", Serializer.LOCATION.serialize(_hopper.getDestination(XHopperDestination.LINKED).get().getLocation())));
        else
            link.setLore(_local.translate("GUI_LINK_DESC_NOLINK"));

        setItemAt(slotToVector(9), breaking.build());
        setItemAt(slotToVector(11), suction.build());
        setItemAt(slotToVector(13), itemFilter.build());
        setItemAt(slotToVector(15), teleport.build());
        setItemAt(slotToVector(17), farming.build());
        setItemAt(slotToVector(31), link.build());
    }

    public String formatEnum(Enum value){
        StringBuilder stringBuilder = new StringBuilder();
        String[] args = value.name().split("_");
        for(String arg : args){
            stringBuilder.append(arg.substring(0, 1).toUpperCase());
            stringBuilder.append(arg.substring(1).toLowerCase());
            stringBuilder.append(' ');
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }
}
