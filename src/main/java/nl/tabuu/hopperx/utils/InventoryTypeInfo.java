package nl.tabuu.hopperx.utils;

import com.google.common.collect.ImmutableMap;
import nl.tabuu.hopperx.hopper.XHopperDestination;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Map;

public enum InventoryTypeInfo {


    // Add linked.
    BEACON(
            Beacon.class,
            false,
            false,
            ImmutableMap.of(
                    0, new Material[] {
                            Material.DIAMOND,
                            Material.EMERALD,
                            Material.GOLD_INGOT,
                            Material.IRON_INGOT
                    }
            ),
            null,
            null
    ),

    BREWING(
            BrewingStand.class,
            true,
            true,
            ImmutableMap.of(
                    3, new Material[] {
                            Material.NETHER_WART,
                            Material.GLOWSTONE_DUST,
                            Material.REDSTONE,
                            Material.FERMENTED_SPIDER_EYE,
                            Material.MAGMA_CREAM,
                            Material.SUGAR,
                            Material.GLISTERING_MELON_SLICE,
                            Material.SPIDER_EYE,
                            Material.GHAST_TEAR,
                            Material.BLAZE_POWDER,
                            Material.GOLDEN_CARROT,
                            Material.PUFFERFISH,
                            Material.RABBIT_FOOT,
                            Material.GUNPOWDER,
                            Material.DRAGON_BREATH
                    },
                    0, new Material[] {
                            Material.POTION,
                            Material.SPLASH_POTION,
                            Material.LINGERING_POTION
                    },
                    1, new Material[] {
                            Material.POTION,
                            Material.SPLASH_POTION,
                            Material.LINGERING_POTION
                    },
                    2, new Material[] {
                            Material.POTION,
                            Material.SPLASH_POTION,
                            Material.LINGERING_POTION
                    }
            ),
            ImmutableMap.of(
                    XHopperDestination.ABOVE, new int[] {
                            3
                    },
                    XHopperDestination.ADJACENT, new int[] {
                            0,
                            1,
                            2
                    },
                    XHopperDestination.LINKED, new int[]{
                            0,
                            1,
                            2
                    }
            ),
            new int[] {
                    0,
                    1,
                    2
            }
     ),

    CHEST(
            Chest.class,
            true,
            true,
            null,
            null,
            null
    ),

    DISPENSER(
            Dispenser.class,
            true,
            true,
            null,
            null,
            null
    ),

    DROPPER(
            Dropper.class,
            true,
            true,
            null,
            null,
            null
    ),

    FURNACE(
            Furnace.class,
            true,
            true,
            ImmutableMap.of(
                    1, Arrays.stream(Material.values()).filter(Material::isFuel).toArray(Material[]::new)
            ),
            ImmutableMap.of(
                    XHopperDestination.BELOW, new int[] {
                            0
                    },
                    XHopperDestination.ADJACENT, new int[] {
                            1
                    },
                    XHopperDestination.LINKED, new int[]{
                            1
                    }
            ),
            new int[] {
                    2
            }
    ),

    HOPPER(
            Hopper.class,
            true,
            true,
            null,
            null,
            null
    ),

    SHULKER_BOX(
            ShulkerBox.class,
            true,
            true,
            null,
            null,
            null
    );

    InventoryTypeInfo(Class clazz, boolean pullable, boolean pushable, Map<Integer, Material[]> whitelist, Map<XHopperDestination, int[]> entrySlots, int[] exitSlots){
        _class      = clazz;
        _pullable   = pullable;
        _pushable   = pushable;
        _whitelist  = whitelist;
        _entrySlots = entrySlots;
        _exitSlots  = exitSlots;
    }

    private Class _class;
    private boolean _pullable, _pushable;
    private Map<XHopperDestination, int[]> _entrySlots;
    private int[] _exitSlots;
    private Map<Integer, Material[]> _whitelist;

    public boolean isPullable() {
        return _pullable;
    }

    public boolean isPushable() {
        return _pushable;
    }

    public boolean hasWhitelist(int slot) {
        if(_whitelist == null)
            return false;
        return _whitelist.get(slot) != null;
    }

    public Material[] getWhitelist(int slot){
        if(_whitelist == null)
            return null;
        return _whitelist.get(slot);
    }

    public int[] getEntrySlots(XHopperDestination hopperPosition){
        if(_entrySlots == null)
            return null;
        return _entrySlots.get(hopperPosition);
    }

    public int[] getExitSlots() {
        return _exitSlots;
    }

    public boolean isExitSlot(int slot){
        if(getExitSlots() == null)
            return true;
        else
            return Arrays.asList(getExitSlots()).contains(slot);
    }

    public Class getContainerClass(){
        return _class;
    }

    public static InventoryTypeInfo getTypeByContainer(Container container){
        for(InventoryTypeInfo type : InventoryTypeInfo.values())
            if(type.getContainerClass().isInstance(container))
                return type;
        return null;
    }

    public static InventoryTypeInfo getTypeByInventory(Inventory inventory){
        try{
            return InventoryTypeInfo.valueOf(inventory.getType().name().toUpperCase());
        }
        catch (IllegalArgumentException e){
            return InventoryTypeInfo.CHEST;
        }
    }
}
