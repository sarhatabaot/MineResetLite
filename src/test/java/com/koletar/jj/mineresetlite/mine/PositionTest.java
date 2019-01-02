package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.mine.Position;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {
    private final Position TEST_POSITION = new Position(11,12,13);
    private final Map<String,Object> TEST_MAP = createMap();
    private final Location TEST_LOCATION = new Location(null,11,12,13);

    private boolean equalsContentPosition(Position p1, Position p2){
        return (p1.getX() == p2.getX() &&  p1.getY() == p2.getY()&&p1.getZ() == p2.getZ());
    }


    private static Map<String,Object> createMap(){
        Map<String,Object> createMap = new HashMap<>();
        createMap.put("x",11);
        createMap.put("y",12);
        createMap.put("z",13);
        return createMap;
    }

    @org.junit.jupiter.api.Test
    void deserialize() {
        assertTrue(equalsContentPosition(TEST_POSITION,Position.deserialize(TEST_POSITION.serialize())));
    }

    @org.junit.jupiter.api.Test
    void toLocation() {
        assertEquals(TEST_LOCATION,TEST_POSITION.toLocation(null));
    }

    @org.junit.jupiter.api.Test
    void serialize() {
        assertEquals(TEST_MAP,TEST_POSITION.serialize());
    }

    @org.junit.jupiter.api.Test
    void getX() {
        assertEquals(11,TEST_POSITION.getX());
    }

    @org.junit.jupiter.api.Test
    void getY() {
        assertEquals(12,TEST_POSITION.getY());
    }

    @org.junit.jupiter.api.Test
    void getZ() {
        assertEquals(13,TEST_POSITION.getZ());
    }

}