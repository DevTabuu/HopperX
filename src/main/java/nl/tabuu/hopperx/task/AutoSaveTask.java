package nl.tabuu.hopperx.task;

import nl.tabuu.hopperx.HopperX;

public class AutoSaveTask implements Runnable {
    @Override
    public void run() {
        HopperX.getInstance().getHopperManager().save();
    }
}
