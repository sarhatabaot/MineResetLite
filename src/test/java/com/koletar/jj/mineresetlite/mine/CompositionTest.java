package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.util.XMaterial;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CompositionTest {
    private final Map<XMaterial, Double> TEST_COMPOSITION_MAP = createCompositionMap();
    private final Composition TEST_COMPOSITION = new Composition(TEST_COMPOSITION_MAP);
    private final Map<XMaterial, Double> TEST_PROBABILITY = createProbabilityMap();
    private final Map<XMaterial, Double> TEST_ADD_COMPOSITION_MAP = createAddCompositionMap();
    private final Map<String,Double> TEST_STRING_COMPOSITION_MAP = createStringCompositionMap();
    private final Map<String,Object> TEST_SERIALIZE_COMPOSITION = createSerializeCompositionMap();

    private static Map<XMaterial, Double> createCompositionMap() {
        HashMap<XMaterial, Double> createMap = new HashMap<>();
        createMap.put(XMaterial.COAL_BLOCK, 0.15);
        createMap.put(XMaterial.ANDESITE, 0.05);
        createMap.put(XMaterial.DIRT, 0.10);
        createMap.put(XMaterial.SAND, 0.20);
        createMap.put(XMaterial.STONE, 0.25);
        return createMap;
    }

    private static Map<XMaterial, Double> createProbabilityMap() {
        HashMap<XMaterial, Double> createMap = new HashMap<>(createCompositionMap());
        createMap.put(XMaterial.AIR, 0.25);
        return createMap;
    }

    private static Map<XMaterial,Double> createAddCompositionMap(){
        HashMap<XMaterial,Double> createMap = new HashMap<>(createCompositionMap());
        createMap.put(XMaterial.GOLD_ORE,5.0);
        return createMap;
    }

    private static Map<String,Object> createSerializeCompositionMap(){
        Map<String,Object> createMap = new HashMap<>();
        createMap.put("blocks",createStringCompositionMap());
        return createMap;
    }

    private static Map<String,Double> createStringCompositionMap(){
        HashMap<String,Double> createMap = new HashMap();
        createMap.put("COAL_BLOCK",0.15);
        createMap.put("ANDESITE",0.05);
        createMap.put("DIRT",0.10);
        createMap.put("SAND",0.20);
        createMap.put("STONE",0.25);
        return createMap;
    }


    @Test
    public void deserialize() {
        Composition c1 = Composition.deserialize(TEST_SERIALIZE_COMPOSITION);
        assertTrue(TEST_COMPOSITION.equals(c1));
    }

    @Test
    public void getProbability() {
        assertEquals(TEST_PROBABILITY,TEST_COMPOSITION.getProbability());
    }

    @Test
    public void add() {
        TEST_COMPOSITION.set(XMaterial.GOLD_ORE,5);
        assertEquals(TEST_ADD_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    public void remove() {
        TEST_COMPOSITION.set(XMaterial.GOLD_ORE,5);
        TEST_COMPOSITION.remove(XMaterial.GOLD_ORE);
        assertEquals(TEST_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    public void getMap() {
        assertEquals(TEST_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    public void calcPercentage() {
        assertEquals(0.75, TEST_COMPOSITION.calcPercentage(),0.01);
    }

    @Test
    public void getTotalPercentage() {
        TEST_COMPOSITION.setTotalPercentage(TEST_COMPOSITION.calcPercentage());
        assertEquals(0.75, TEST_COMPOSITION.getTotalPercentage(),0.01);
    }


    @Test
    public void parseString() {
        assertEquals(TEST_STRING_COMPOSITION_MAP,TEST_COMPOSITION.parseString());
    }

    @Test
    public void isMaterial() {
        assertTrue(TEST_COMPOSITION.isMaterial(XMaterial.COAL_BLOCK));
        assertFalse(TEST_COMPOSITION.isMaterial(XMaterial.DARK_OAK_LOG));
    }

    @Test
    public void serialize() {
        assertEquals(TEST_SERIALIZE_COMPOSITION,TEST_COMPOSITION.serialize());
    }
}