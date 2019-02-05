package nl.tabuu.hopperx;

import nl.tabuu.hopperx.hopper.XHopperManager;
import nl.tabuu.hopperx.listener.HopperListeners;
import nl.tabuu.hopperx.listener.PlayerListeners;
import nl.tabuu.tabuucore.plugin.TabuuCorePlugin;

public class HopperX extends TabuuCorePlugin {

    private static HopperX _instance;
    private XHopperManager _hopperManager;

    @Override
    public void onEnable(){
        _instance = this;

        getConfigurationManager().addConfiguration("config");
        getConfigurationManager().addConfiguration("hopper-data");
        getConfigurationManager().addConfiguration("lang");

        _hopperManager = new XHopperManager();
        this.getServer().getPluginManager().registerEvents(new HopperListeners(), getInstance());
        this.getServer().getPluginManager().registerEvents(new PlayerListeners(), getInstance());
    }

    @Override
    public void onDisable(){
        _hopperManager.save();
    }

    public XHopperManager getHopperManager(){
        return _hopperManager;
    }

    public static HopperX getInstance(){
        return _instance;
    }
}
