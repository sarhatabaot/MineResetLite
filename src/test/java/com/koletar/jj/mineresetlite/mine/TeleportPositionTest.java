package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.data.MineData;
import com.koletar.jj.mineresetlite.mine.TeleportPosition;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeleportPositionTest extends PositionTest {
    private final TeleportPosition TEST_TELEPORTPOSITION = new TeleportPosition(11,12,13,14,15);
    private final Location TEST_LOCATION = new Location(null,11,12,13,14,15);
    private final Map<String,Object> TEST_MAP = createMap();

    private static Map<String,Object> createMap(){
        HashMap<String,Object> createMap = new HashMap<>();
        createMap.put("x",11);
        createMap.put("y",12);
        createMap.put("z",13);
        createMap.put("yaw",14);
        createMap.put("pitch",15);
        return createMap;
    }

    @Test
    void deserialize() {
    }

    @Test
    void toLocation() {
        assertEquals(TEST_LOCATION,TEST_TELEPORTPOSITION.toLocation(null));
    }

    @Test
    void serialize() {
        assertEquals(TEST_MAP,TEST_TELEPORTPOSITION.serialize());
    }

    @Test
    void getYaw() {
        assertEquals(14,TEST_TELEPORTPOSITION.getYaw());
    }

    @Test
    void getPitch() {
        assertEquals(15,TEST_TELEPORTPOSITION.getPitch());
    }


}