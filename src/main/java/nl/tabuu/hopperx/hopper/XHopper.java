package nl.tabuu.hopperx.hopper;

import com.google.common.collect.ImmutableMap;
import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.database.HopperValue;
import nl.tabuu.hopperx.farming.FarmableBlockStack;
import nl.tabuu.hopperx.gui.HopperGUI;
import nl.tabuu.hopperx.utils.InventoryTypeInfo;
import nl.tabuu.hopperx.utils.Methods;
import nl.tabuu.tabuucore.item.ItemList;
import nl.tabuu.tabuucore.util.BukkitUtils;
import nl.tabuu.tabuucore.util.Random;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

public class XHopper {

    private Hopper _hopper;
    private Map<XHopperDestination, Optional<Inventory>> _destinations;
    private List<Material> _whitelist, _voidFilter;
    private double _suctionRange;
    private int _speed;
    private boolean _active, _blockBreaking, _teleporting, _sucking, _filter, _farming;

    private double _itemMoveAmount;

    public XHopper(
                    Hopper                                          hopper,
                    Map<XHopperDestination, Optional<Inventory>>    destinations,
                    List<Material>                                  whitelist,
                    List<Material>                                  voidFilter,
                    double                                          suctionRange,
                    int                                             speed,
                    boolean                                         blockBreaking,
                    boolean                                         teleporting,
                    boolean                                         sucking,
                    boolean                                         filter,
                    boolean                                         farming
    ) {
        _hopper             = hopper;
        _destinations       = destinations;
        _whitelist          = whitelist;
        _voidFilter         = voidFilter;
        _suctionRange       = suctionRange;
        _speed              = speed;
        _active             = false;
        _blockBreaking      = blockBreaking;
        _teleporting        = teleporting;
        _sucking            = sucking;
        _filter             = filter;
        _farming            = farming;

        _itemMoveAmount = 0;

        for(XHopperDestination hopperDestination : XHopperDestination.values()){
            if(!_destinations.containsKey(hopperDestination)) {
                _destinations.put(hopperDestination, Optional.empty());
            }
        }

    }

    public XHopper(Hopper hopper){
        this(
                hopper,
                new LinkedHashMap<>(),
                (List<Material>) HopperValue.FILTER_WHITELIST.getDefault().orElse(new ArrayList<>()),
                (List<Material>) HopperValue.FILTER_VOIDLIST.getDefault().orElse(new ArrayList<>()),
                (double) HopperValue.SUCTION_RANGE.getDefault().orElse(0d),
                (int) HopperValue.SPEED.getDefault().orElse(0),
                (boolean) HopperValue.BLOCK_BREAKING.getDefault().orElse(false),
                (boolean) HopperValue.TELEPORTING.getDefault().orElse(false),
                (boolean) HopperValue.SUCKING.getDefault().orElse(false),
                (boolean) HopperValue.FILTER.getDefault().orElse(false),
                (boolean) HopperValue.FARMING.getDefault().orElse(false)
        );
    }

    // Fires when it's queued
    public void update(double deltaTime){
        detectDestinations();
        _itemMoveAmount += _speed * deltaTime;

        if(_itemMoveAmount >= 1){
            int toMove = (int) Math.floor(_itemMoveAmount);
            push(toMove);
            pull(toMove);
            _itemMoveAmount -= toMove;
        }

        if(_blockBreaking)
            breakBlock();

        if(_sucking)
            suck();

        if(_farming)
            farm();
    }

    public void push(int itemCount){
        ItemList removableItems = new ItemList();
        removableItems.stackAll(_hopper.getInventory().getContents());

        if(isFilter()){
            if(!getVoidFilter().isEmpty()){
                ItemList inventoryClone = new ItemList();
                inventoryClone.addAll(_hopper.getInventory().getContents().clone());

                boolean update = false;
                removableItems.removeIf(i -> isVoidFiltered(i));
                for(Material voidMaterial : _voidFilter){
                    if(itemCount <=0)
                        break;

                    int count = inventoryClone.remove(voidMaterial, itemCount);
                    if(count != itemCount){
                        itemCount = count;
                        update = true;
                    }
                }
                if(update)
                    _hopper.getInventory().setContents(inventoryClone.stream().toArray(ItemStack[]::new));
            }
        }

        ItemList removedItems = Methods.hopperTransfer(getPushDestinations(), removableItems.clone(), itemCount);
        _hopper.getInventory().removeItem(removedItems.stream().toArray(ItemStack[]::new));
    }

    public void pull(int itemCount){
        if(!_destinations.get(XHopperDestination.ABOVE).isPresent())
            return;

        Inventory inventory = _destinations.get(XHopperDestination.ABOVE).get();
        InventoryTypeInfo info = InventoryTypeInfo.getTypeByInventory(inventory);
        ItemList contents = new ItemList();

        if(info.getExitSlots() != null)
            for(int slot : info.getExitSlots())
                contents.stack(inventory.getItem(slot));
        else
            contents.stackAll(inventory.getContents());

        contents.removeIf(i -> !isWhitelisted(i));

        Map<XHopperDestination, Optional<Inventory>> destinations = ImmutableMap.of(
                XHopperDestination.ABOVE, Optional.of(_hopper.getInventory())
        );

        ItemList removedItems = Methods.hopperTransfer(destinations, contents.clone(), itemCount);
        inventory.removeItem(removedItems.stream().toArray(ItemStack[]::new));
    }

    public void breakBlock(){
        Block block = _hopper.getBlock().getRelative(BlockFace.UP);

        if(block.isEmpty())
            return;

        if(!HopperX.getInstance().getHopperManager().getUnbreakableBlocks().contains(block.getType())){
            ItemList drops = new ItemList();
            drops.stackAll(block.getDrops(new ItemStack(Material.DIAMOND_PICKAXE)));

            ItemList dropToGround = new ItemList();
            dropToGround.addAll(drops.stream().filter(i -> !isWhitelisted(i)).collect(Collectors.toList()));
            drops.removeIf(i -> !isWhitelisted(i));

            ItemList list = new ItemList();
            list.addAll(_hopper.getInventory().getContents());

            if(list.clone().stackAll(drops.clone()).isEmpty()){
                _hopper.getInventory().addItem(drops.stream().toArray(ItemStack[]::new));
                block.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation().clone().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0);
                block.getLocation().getWorld().playSound(block.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
                block.setType(Material.AIR);

                for(ItemStack item : dropToGround)
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        }
    }

    public void suck(){
        double r = _suctionRange;
        List<Item> groundItems = _hopper.getWorld().getNearbyEntities(_hopper.getLocation(), r, r, r)
                .stream()
                .filter(Item.class::isInstance)
                .map(Item.class::cast)
                .filter(item -> (item.getCustomName() == null || !item.getCustomName().equals("sucked")) && isWhitelisted(item.getItemStack()))
                .collect(Collectors.toList());

        List<Integer> indexes = new ArrayList<>(_hopper.getInventory().addItem(groundItems.stream().map(Item::getItemStack).toArray(ItemStack[]::new)).keySet());

        indexes.sort(Comparator.reverseOrder());
        indexes.stream().mapToInt(i -> i).forEach(groundItems::remove);

        for(Item item : groundItems){
            item.setCustomName("sucked");
            item.remove();
            item.getLocation().getWorld().spawnParticle(Particle.FLAME, item.getLocation(), 5, 0.5, 0.5, 0.5, 0);
        }
    }

    public void farm(){
        ItemList drops = new ItemList();
        List<Block> blocksInRange = BukkitUtils.getBlocksInRadius(getLocation().add(0, 1, 0), 4, 1, 4);
        ItemStack farmTool = new ItemStack(Material.DIAMOND_HOE);

        for(Block block : blocksInRange){
            boolean farmed = true;
            if(block.getBlockData() instanceof Ageable && !(block.getType().equals(Material.SUGAR_CANE) || block.getType().equals(Material.CACTUS))){
                if(block.getType().name().endsWith("_STEM"))
                    continue;
                Ageable ageable = (Ageable) block.getBlockData();
                if(ageable.getAge() >= ageable.getMaximumAge()){
                    // Somehow not all crops return drops
                    switch (block.getType()){
                        case COCOA:
                            drops.add(new ItemStack(Material.COCOA_BEANS, (int) Math.round(Random.range(2, 3))));
                            break;

                        case NETHER_WART:
                            drops.add(new ItemStack(Material.NETHER_WART, (int) Math.round(Random.range(2, 4))));
                            break;

                        default:
                            drops.addAll(block.getDrops(farmTool));
                            break;
                    }

                    ageable.setAge(0);
                    block.setBlockData(ageable);
                }
                else farmed = false;
            }
            else if(block.getType().equals(Material.MELON) || block.getType().equals(Material.PUMPKIN)){
                drops.addAll(block.getDrops(farmTool));
                block.setType(Material.AIR, true);
            }
            else if(block.getType().equals(Material.SUGAR_CANE) || block.getType().equals(Material.CACTUS)){
                //TODO: Sort list?
                FarmableBlockStack blockStack = new FarmableBlockStack(block);
                if(blockStack.getStackSize() > 1)
                    drops.addAll(blockStack.farm(farmTool, false));
                else
                    farmed = false;
            }
            else
                farmed = false;

            if(farmed)
                block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.5, 0.5, 0.5, 0);
        }

        drops.removeIf(item -> item.getType().name().endsWith("_SEEDS"));
        ItemList nonFitting = new ItemList();
        nonFitting.addAll(drops.stream().filter(i -> !isWhitelisted(i)).collect(Collectors.toList()));
        drops.removeIf(i -> !isWhitelisted(i));
        nonFitting.addAll(_hopper.getInventory().addItem(drops.stream().toArray(ItemStack[]::new)).values());
        nonFitting.forEach(item -> getLocation().getWorld().dropItemNaturally(getLocation().add(0.5, 0.5, 0.5), item));
    }

    public void detectDestinations(){
        _hopper = (Hopper) _hopper.getLocation().getBlock().getState();

        BlockFace blockFace = ((org.bukkit.material.Hopper)_hopper.getData()).getFacing();
        BlockState adjacentBlockState = _hopper.getBlock().getRelative(blockFace).getState();
        BlockState belowBlockState = _hopper.getBlock().getRelative(BlockFace.DOWN).getState();
        BlockState aboveBlockState = _hopper.getBlock().getRelative(BlockFace.UP).getState();


        if(adjacentBlockState instanceof Container && (!blockFace.equals(BlockFace.UP) && !blockFace.equals(BlockFace.DOWN)))
            setDestination(XHopperDestination.ADJACENT, ((Container) adjacentBlockState).getInventory());
        else
            setDestination(XHopperDestination.ADJACENT, null);

        if(belowBlockState instanceof Container)
            setDestination(XHopperDestination.BELOW, ((Container) belowBlockState).getInventory());
        else
            setDestination(XHopperDestination.BELOW, null);

        if(aboveBlockState instanceof Container && !(aboveBlockState instanceof Hopper))
            setDestination(XHopperDestination.ABOVE, ((Container) aboveBlockState).getInventory());
        else
            setDestination(XHopperDestination.ABOVE, null);

        for(Map.Entry<XHopperDestination, Optional<Inventory>> entry : _destinations.entrySet()){
            if(!entry.getValue().isPresent())
                continue;
            BlockState blockState = entry.getValue().get().getLocation().getBlock().getState();
            if (blockState instanceof Container)
                entry.setValue(Optional.of(((Container) blockState).getInventory()));
            else
                entry.setValue(Optional.empty());
        }
    }

    public void openGui(Player player){
        player.openInventory(new HopperGUI(this).getInventory());
    }

    // Getters and setters
    public Location getLocation(){
        return _hopper.getLocation();
    }

    public void setActive(boolean value){
        _active = value;
    }

    public boolean isActive(){
        return _active;
    }

    public Optional<Inventory> getDestination(XHopperDestination destination){
        return _destinations.get(destination);
    }

    public Hopper getHopper(){
        return _hopper;
    }

    public void setDestination(XHopperDestination destination, Inventory inventory){
        _destinations.put(destination, inventory == null ? Optional.empty() : Optional.of(inventory));
    }

    public LinkedHashMap<XHopperDestination, Optional<Inventory>> getPushDestinations(){
        LinkedHashMap<XHopperDestination, Optional<Inventory>> destinations = new LinkedHashMap<>();

        for(int i = 0; i < 3; i++)
            destinations.put(XHopperDestination.values()[i], getDestination(XHopperDestination.values()[i]));

        return destinations;
    }

    public List<Material> getWhitelist() {
        return _whitelist;
    }

    public void setWhitelist(List<Material> whitelist) {
        _whitelist = whitelist;
    }

    public boolean isWhitelisted(Material material){
        return _whitelist.isEmpty() || !_filter || (_filter && _whitelist.contains(material));
    }

    public boolean isWhitelisted(ItemStack itemStack){
        return isWhitelisted(itemStack.getType());
    }

    public List<Material> getVoidFilter() {
        return _voidFilter;
    }

    public void setVoidFilter(List<Material> voidFilter) {
        _voidFilter = voidFilter;
    }

    public boolean isVoidFiltered(Material material){
        return _filter && !_voidFilter.isEmpty() && _voidFilter.contains(material);
    }

    public boolean isVoidFiltered(ItemStack itemStack){
        return isVoidFiltered(itemStack.getType());
    }

    public double getSuctionRange() {
        return _suctionRange;
    }

    public void setSuctionRange(double suctionRange) {
        _suctionRange = suctionRange;
    }

    public int getSpeed() {
        return _speed;
    }

    public void setSpeed(int speed) {
        _speed = speed;
    }

    public boolean isBlockBreaking() {
        return _blockBreaking;
    }

    public void setBlockBreaking(boolean blockBreaking) {
        _blockBreaking = blockBreaking;
    }

    public boolean isTeleporting() {
        return _teleporting;
    }

    public void setTeleporting(boolean teleporting) {
        _teleporting = teleporting;
    }

    public boolean isSucking() {
        return _sucking;
    }

    public void setSucking(boolean sucking) {
        _sucking = sucking;
    }

    public boolean isFilter() {
        return _filter;
    }

    public void setFilter(boolean filter) {
        _filter = filter;
    }

    public boolean isFarming(){
        return _farming;
    }

    public void setFarming(boolean farming){
        _farming = farming;
    }


    public Object getValueByHopperValue(HopperValue hopperValue){
        switch (hopperValue){
            case DESTINATION_BLACKLIST:
                Optional<Inventory> blacklistInv = getDestination(XHopperDestination.BLACKLIST);
                return blacklistInv.map(Inventory::getLocation).orElse(null);

            case DESTINATION_LINKED:
                Optional<Inventory> linkedInv = getDestination(XHopperDestination.LINKED);
                return linkedInv.map(Inventory::getLocation).orElse(null);

            case MAX_LINK_DISTANCE:
                return HopperX.getInstance().getConfigurationManager().getConfiguration("config").getInt("MAX_LINK_DISTANCE");

            case FILTER_WHITELIST:
                return getWhitelist();

            case FILTER_VOIDLIST:
                return getVoidFilter();

            case BLOCK_BREAKING:
                return isBlockBreaking();

            case SUCTION_RANGE:
                return getSuctionRange();

            case TELEPORTING:
                return isTeleporting();

            case SUCKING:
                return isSucking();

            case SPEED:
                return getSpeed();

            case FILTER:
                return isFilter();

            case FARMING:
                return isFarming();

            default:
                return null;
        }
    }

    public void setValueByHopperValue(HopperValue hopperValue, Object value){
        switch(hopperValue){
            case DESTINATION_BLACKLIST:
                setDestination(XHopperDestination.BLACKLIST, (Inventory) value);
                break;

            case DESTINATION_LINKED:
                setDestination(XHopperDestination.LINKED, (Inventory) value);
                break;

            case MAX_LINK_DISTANCE:
                break;

            case FILTER_WHITELIST:
                setWhitelist((List<Material>) value);
                break;

            case FILTER_VOIDLIST:
                setVoidFilter((List<Material>) value);

            case BLOCK_BREAKING:
                setBlockBreaking((boolean) value);
                break;

            case SUCTION_RANGE:
                setSuctionRange((double) value);
                break;

            case TELEPORTING:
                setTeleporting((boolean) value);
                break;

            case SUCKING:
                setSucking((boolean) value);
                break;

            case SPEED:
                setSpeed((int) value);
                break;

            case FARMING:
                setFarming((boolean) value);

            case FILTER:
                setFilter((boolean) value);
        }
    }
}
