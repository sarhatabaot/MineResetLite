package com.koletar.jj.mineresetlite.events;

import com.koletar.jj.mineresetlite.Mine;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MineUpdatedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private Mine mine;
    public MineUpdatedEvent(Mine mine) {
        super();
        this.mine = mine;
    }

    public Mine getMine() {
        return this.mine;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
