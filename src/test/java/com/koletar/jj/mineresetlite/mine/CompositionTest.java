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


    @Test
    void deserialize() {
    }

    @Test
    void getProbability() {
    }

    @Test
    void add() {
        TEST_COMPOSITION.add(XMaterial.GOLD_ORE,5);
        assertEquals(TEST_ADD_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    void remove() {
        TEST_COMPOSITION.add(XMaterial.GOLD_ORE,5);
        TEST_COMPOSITION.remove(XMaterial.GOLD_ORE);
        assertEquals(TEST_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    void getMap() {
        assertEquals(TEST_COMPOSITION_MAP,TEST_COMPOSITION.getMap());
    }

    @Test
    void calcPercentage() {
        assertEquals(0.75, TEST_COMPOSITION.calcPercentage(),0.01);
    }

    @Test
    void getTotalPercentage() {
        TEST_COMPOSITION.setTotalPercentage(TEST_COMPOSITION.calcPercentage());
        assertEquals(0.75, TEST_COMPOSITION.getTotalPercentage(),0.01);
    }


    @Test
    void parseString() {
    }

    @Test
    void isMaterial() {
    }

    @Test
    void serialize() {
    }
}