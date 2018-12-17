package com.koletar.jj.mineresetlite;

import java.util.HashMap;
import java.util.Map;

public class TeleportPosition extends Position {
    private int pitch;
    private int yaw;

    public TeleportPosition(){
        super();
        this.pitch = 0;
        this.yaw = 0;
    }
    public TeleportPosition(int x, int y, int z, int pitch, int yaw){
        super(x,y,z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public TeleportPosition(Map<String,Object> map){
        super(map);
        this.pitch = (Integer) map.get("pitch");
        this.yaw = (Integer) map.get("yaw");
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String,Object> mapSerializer = new HashMap<>(super.serialize());
        mapSerializer.put("pitch",this.pitch);
        mapSerializer.put("yaw",this.yaw);
        return mapSerializer;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    @Override
    public String toString() {
        return "TeleportPosition{" +
                "pitch=" + pitch +
                ", yaw=" + yaw +
                "} " + super.toString();
    }
}
