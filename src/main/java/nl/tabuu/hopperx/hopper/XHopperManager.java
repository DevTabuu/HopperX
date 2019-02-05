package nl.tabuu.hopperx.hopper;

import nl.tabuu.hopperx.HopperX;
import nl.tabuu.hopperx.database.HopperConfig;
import nl.tabuu.hopperx.database.IHopperDatabase;
import nl.tabuu.hopperx.task.AutoSaveTask;
import nl.tabuu.hopperx.task.HopperTask;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import nl.tabuu.tabuucore.util.Dictionary;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class XHopperManager {

    private HashMap<UUID, HopperLinkWrapper> _linkProcesses;
    private HashMap<Location, XHopper> _hoppers;
    private HashMap<Vector, Integer> _hopperChunkCount;
    private Queue<HopperTask> _hopperTasks;
    private Dictionary _local;
    private IHopperDatabase _database;
    private IConfiguration _config;
    private int _autoSaveTaskId;
    private int _maxQueueSize;

    public XHopperManager(){
        _linkProcesses      = new HashMap<>();
        _hoppers            = new HashMap<>();
        _hopperChunkCount   = new HashMap<>();
        _hopperTasks        = new LinkedBlockingQueue<>();
        _local              = HopperX.getInstance().getConfigurationManager().getConfiguration("lang").getDictionary("");
        _database           = new HopperConfig();
        _config             = HopperX.getInstance().getConfigurationManager().getConfiguration("config");
        _autoSaveTaskId     = -1;
        _maxQueueSize       = _config.getInt("HopperTasksPerTick");

        setAutoSave(_config.getTime("AutoSaveInterval"));
        load();
        calcHopperPerChunk();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(HopperX.getInstance(), this::doHopperTick, 1L, 1L);
    }

    private void doHopperTick(){
        for(int i = 0; i < _hopperTasks.size() && i < _maxQueueSize; i++){
            if(_hopperTasks.isEmpty())
                break;

            HopperTask hopperTask = _hopperTasks.poll();

            hopperTask.run();
            hopperTask.reset();

            if(isRegistered(hopperTask.getHopper()))
                _hopperTasks.offer(hopperTask);
        }
    }

    public void load(){
        _database.load().forEach(this::register);
    }

    public void calcHopperPerChunk(){
        _hopperChunkCount.clear();
        _hoppers.keySet().forEach(location -> {
            int value = getHopperChunkCount(location.getChunk());
            value++;
            setHopperChunkCount(location.getChunk(), value);
        });
    }

    private void setHopperChunkCount(Chunk chunk, int amount){
        Vector key = new Vector(chunk.getX(), 0, chunk.getZ());
        _hopperChunkCount.put(key, amount);
    }

    private int getHopperChunkCount(Chunk chunk){
        Vector key = new Vector(chunk.getX(), 0, chunk.getZ());
        return _hopperChunkCount.getOrDefault(key, 0);
    }

    public void save(){
        _database.save(_hoppers);
    }

    public XHopper getHopperAt(Location location){
        return _hoppers.get(location);
    }

    private void register(Location location, XHopper hopper){
        if(!isRegistered(hopper)){
            _hoppers.put(location, hopper);
            _hopperTasks.offer(new HopperTask(hopper));

            int value = getHopperChunkCount(location.getChunk());
            value++;
            setHopperChunkCount(location.getChunk(), value);
        }
    }

    public void register(XHopper hopper){
        register(hopper.getLocation(), hopper);
    }

    public void register(Hopper hopper){
        register(new XHopper(hopper));
    }

    private void unregister(Location location){
        if(isRegistered(location)){
            _hoppers.remove(location);

            int value = getHopperChunkCount(location.getChunk());
            value--;
            setHopperChunkCount(location.getChunk(), value);
        }
    }

    public void unregister(XHopper hopper){
        unregister(hopper.getLocation());
    }

    public void unregister(Hopper hopper){
        unregister(hopper.getLocation());
    }

    private boolean isRegistered(Location location){
        return _hoppers.containsKey(location);
    }

    public boolean isRegistered(XHopper hopper){
        return isRegistered(hopper.getLocation());
    }

    public boolean isRegistered(Hopper hopper){
        return isRegistered(hopper.getLocation());
    }

    public Collection<XHopper> getHoppers(){
        return _hoppers.values();
    }

    public boolean isLinking(Player player){
        return getLinking(player) != null;
    }

    public HopperLinkWrapper getLinking(Player player){
        return _linkProcesses.get(player.getUniqueId());
    }

    public void startLinking(Player player, XHopper xHopper, XHopperDestination destination){
        HopperLinkWrapper wrapper = new HopperLinkWrapper(player.getUniqueId(), System.currentTimeMillis(), xHopper, destination);
        long linkTimeout = _config.getTime("LinkTimeout") / 50;

        _linkProcesses.put(player.getUniqueId(), wrapper);
        player.sendMessage(_local.translate("LINK_START"));

        Bukkit.getScheduler().runTaskLater(HopperX.getInstance(), () -> {
            if(_linkProcesses.remove(player.getUniqueId(), wrapper))
                player.sendMessage(_local.translate("LINK_TIMEOUT"));
        }, linkTimeout);
    }

    public void finishLinking(Player player){
        _linkProcesses.remove(player.getUniqueId());
    }

    public boolean canPlaceHopperAt(Location location){
        Vector key = new Vector(location.getChunk().getX(), 0, location.getChunk().getZ());
        int currentCount = _hopperChunkCount.getOrDefault(key, 0);
        return currentCount < _config.getInt("MaxHoppersPerChunk");
    }

    public void setAutoSave(long autoSaveInterval){
        if(autoSaveInterval > 0) {
            long autoSaveIntervalTicks = autoSaveInterval / 50;
            _autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(HopperX.getInstance(), new AutoSaveTask(), autoSaveIntervalTicks, autoSaveIntervalTicks);
        }
        else if(_autoSaveTaskId != -1){
            Bukkit.getScheduler().cancelTask(_autoSaveTaskId);
            _autoSaveTaskId = -1;
        }
    }

    public List<Material> getUnbreakableBlocks(){
        return _config.getEnumList(Material.class, "UnbreakableBlocks");
    }
}
