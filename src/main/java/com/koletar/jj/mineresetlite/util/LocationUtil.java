package com.koletar.jj.mineresetlite.util;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class LocationUtil {
    /**
     *
     * @param location location to serialize
     * @param include include pitch and yaw. true = include, false = don't
     * @return Map of serialized location
     */
    public static Map<String,Object> serialize(Location location, boolean include){
        HashMap<String,Object> serializedLocation = new HashMap<>();
        serializedLocation.put("x",location.getBlockX());
        serializedLocation.put("y",location.getBlockY());
        serializedLocation.put("z",location.getBlockZ());
        if(include) {
            serializedLocation.put("pitch",Math.round(location.getPitch()));
            serializedLocation.put("yaw",Math.round(location.getYaw()));
        }
        return serializedLocation;
    }
}
