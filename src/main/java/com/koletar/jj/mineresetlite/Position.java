package com.koletar.jj.mineresetlite;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class Position implements ConfigurationSerializable {
    private int x,y,z;

    public Position(){
        this.x = 0;
        this.y = -Integer.MAX_VALUE;
        this.z = 0;
    }
    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Map<String,Object> map){
        this.x = (Integer) map.get("x");
        this.y = (Integer) map.get("y");
        this.z = (Integer) map.get("z");
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String,Object> mapSerializer = new HashMap<>();
        mapSerializer.put("x",this.x);
        mapSerializer.put("y",this.y);
        mapSerializer.put("z",this.z);
        return mapSerializer;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
