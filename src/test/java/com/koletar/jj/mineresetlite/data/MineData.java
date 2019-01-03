package com.koletar.jj.mineresetlite.data;

import com.koletar.jj.mineresetlite.mine.Composition;
import com.koletar.jj.mineresetlite.mine.Mine;
import com.koletar.jj.mineresetlite.mine.Position;
import com.koletar.jj.mineresetlite.mine.TeleportPosition;
import com.koletar.jj.mineresetlite.util.XMaterial;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class for keeping all mock data.
 *
 */
public class MineData {
    private static final Position TEST_MINPOS = new Position(createPosMap(11,12,13));
    private static final Position TEST_MAXPOS = new Position(createPosMap(21,22,23));
    private static final TeleportPosition TEST_TPPOS = new TeleportPosition(11,12,13,14,15);
    private static final String TEST_MINE_NAME = "TEST";

    private static final Mine TEST_MINE = new Mine(TEST_MINPOS,TEST_MAXPOS,TEST_MINE_NAME,null);

    private static final Map<XMaterial,Double> TEST_COMPOSITION_MAP = createCompositionMap();

    private static final Composition TEST_COMPOSITION = new Composition(TEST_COMPOSITION_MAP);

    public static Position getTEST_MINPOS() {
        return TEST_MINPOS;
    }

    public static Position getTEST_MAXPOS() {
        return TEST_MAXPOS;
    }

    public static TeleportPosition getTEST_TPPOS() {
        return TEST_TPPOS;
    }

    public static String getTEST_MINE_NAME() {
        return TEST_MINE_NAME;
    }

    public static Mine getTEST_MINE() {
        return TEST_MINE;
    }

    public static Map<XMaterial, Double> getTEST_COMPOSITION_MAP() {
        return TEST_COMPOSITION_MAP;
    }

    public static Composition getTEST_COMPOSITION() {
        return TEST_COMPOSITION;
    }

    private static Map<String,Object> createPosMap(int x, int y, int z){
        Map<String,Object> createMap = new HashMap<>();
        createMap.put("x",x);
        createMap.put("y",y);
        createMap.put("z",z);
        return createMap;
    }

    private static Map<String,Object> createTeleportPosMap(){
        HashMap<String,Object> createMap = new HashMap<>();
        createMap.put("x",11);
        createMap.put("y",12);
        createMap.put("z",13);
        createMap.put("yaw",14);
        createMap.put("pitch",15);
        return createMap;
    }


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

}
