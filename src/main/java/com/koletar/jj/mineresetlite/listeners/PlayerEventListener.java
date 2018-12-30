package com.koletar.jj.mineresetlite.listeners;

import com.koletar.jj.mineresetlite.MineResetLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerEventListener implements Listener {
    private MineResetLite plugin;

    public PlayerEventListener(MineResetLite plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if (event.getPlayer().hasPermission("mineresetlite.updates") && plugin.isNeedsUpdate()) {
            event.getPlayer().sendMessage("\u00A7f[\u00A74MRL\u00A7f] New update found: "+plugin.getNewVersion()+" You are running: "+plugin.getDescription().getVersion());
            event.getPlayer().sendMessage("\u00A7f[\u00A74MRL\u00A7f] Update at: https://github.com/sarhatabaot/MineResetLite/releases");
        }
    }


}
