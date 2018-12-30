package com.koletar.jj.mineresetlite.listeners;

import com.koletar.jj.mineresetlite.MineResetLite;
import com.koletar.jj.mineresetlite.mine.Mine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BrokenBlockEventListener implements Listener {
    private MineResetLite plugin;


    public BrokenBlockEventListener(MineResetLite plugin) {
        this.plugin = plugin;
    }

    /**
     * When a player breaks a block inside a mine, adds to <code>mine.getBrokenBlocks()</code>.
     * @param event     Block break event.
     */
    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event){
        for(Mine mine: plugin.mines){
            if (mine.isInside(event.getPlayer())){
                mine.setBrokenBlocks(mine.getBrokenBlocks()+1);
            }
        }
    }

    /**
     * When a player places a block inside a mine, subtract from <code>mine.getBrokenBlocks()</code>.
     * @param event     Block place event.
     */
    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        for(Mine mine: plugin.mines){
            if (mine.isInside(event.getPlayer())){
                mine.setBrokenBlocks(mine.getBrokenBlocks()-1);
            }
        }
    }
}
