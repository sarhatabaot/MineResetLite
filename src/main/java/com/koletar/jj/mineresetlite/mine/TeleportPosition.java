package com.koletar.jj.mineresetlite.mine;

import org.bukkit.Location;
import org.bukkit.World;

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

    public TeleportPosition(Location location){
        super(location);
        this.pitch = Math.round(location.getPitch());
        this.yaw = Math.round(location.getYaw());
    }

    public static TeleportPosition deserialize(Map<String, Object> map){
        int x = (Integer) map.get("x");
        int y = (Integer) map.get("y");
        int z = (Integer) map.get("z");
        int pitch = (Integer) map.get("pitch");
        int yaw = (Integer) map.get("yaw");
        return new TeleportPosition(x,y,z,pitch,yaw);
    }
    public TeleportPosition(Map<String,Object> map){
        super(map);
        this.pitch = (Integer) map.get("pitch");
        this.yaw = (Integer) map.get("yaw");
    }

    public Location toLocation(World world){
        return new Location(world,this.getX(),this.getY(),this.getZ(),this.yaw,this.pitch);
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
