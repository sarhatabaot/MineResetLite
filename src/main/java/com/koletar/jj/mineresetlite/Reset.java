package com.koletar.jj.mineresetlite;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

public class Reset implements ConfigurationSerializable {
    private int delay;
    transient private int timer;
    private List<Integer> warnings;
    private boolean isSilent;

    public Reset(){

    }

    public Reset(int delay,List<Integer> warnings, boolean isSilent){
        this.delay = delay;
        this.warnings = new LinkedList<>(warnings);
        this.isSilent = isSilent;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> mapSerializer = new HashMap<>();
        mapSerializer.put("delay",this.delay);
        mapSerializer.put("isSilent",this.isSilent);
        mapSerializer.put("warnings",this.warnings);
        return mapSerializer;
    }

    public List<Integer> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public void setWarnings(List<Integer> warnings) {
        this.warnings = new ArrayList<>(warnings);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int minutes) {
        this.delay = minutes;
        this.timer = minutes;
    }

    public int getTimer() {
        return timer;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean silent) {
        isSilent = silent;
    }

}
