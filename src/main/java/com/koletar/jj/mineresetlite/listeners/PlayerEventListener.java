package com.koletar.jj.mineresetlite.listeners;

import com.koletar.jj.mineresetlite.MineResetLite;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static com.koletar.jj.mineresetlite.util.Phrases.phrase;

public class PlayerEventListener implements Listener {
    private MineResetLite plugin;

    public PlayerEventListener(MineResetLite plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event){
        if (event.getPlayer().hasPermission("mineresetlite.updates") && plugin.isNeedsUpdate()) {
            event.getPlayer().sendMessage(phrase("updateWarning1"));
            event.getPlayer().sendMessage(phrase("updateWarning2"));
        }
    }


}
