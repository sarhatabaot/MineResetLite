package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.util.XMaterial;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class Composition implements ConfigurationSerializable {
    private Map<XMaterial,Double> composition;
    private double totalPercentage;

    public Composition(){
        this.composition = new HashMap<>();
    }

    public static Composition deserialize(Map<String,Object> me){
        Map<String,Double> stringDoubleMap = (Map<String, Double>) me.get("blocks");
        Map<XMaterial,Double> comp = new HashMap<>();
        for(Map.Entry<String,Double> entry: stringDoubleMap.entrySet()){
            comp.put(XMaterial.fromString(entry.getKey()),entry.getValue());
        }
        return new Composition(comp);
    }

    public Composition(Map<XMaterial, Double> composition) {
        this.composition = composition;
    }

    public Map<XMaterial,Double> getProbability(){
        Map<XMaterial,Double> paddedComposition = new HashMap<>(this.composition);
        this.totalPercentage = calcPercentage();
        padComposition(paddedComposition);
        return generateProbabilityMap(paddedComposition);
    }

    private double calcPercentage(){
        double percentage = 0;
        for(Map.Entry<XMaterial,Double> entry: composition.entrySet()){
            percentage += entry.getValue();
        }
        return percentage;
    }

    private void padComposition(Map<XMaterial,Double> composition){
        if(this.totalPercentage < 1){
            composition.put(XMaterial.AIR, 1 - this.totalPercentage);
            this.totalPercentage = 1;
        }
    }

    private Map<XMaterial,Double> generateProbabilityMap(Map<XMaterial,Double> paddedComposition){
        HashMap<XMaterial,Double> probability = new HashMap<>();
        double i = 0;
        for(Map.Entry<XMaterial,Double> entry : paddedComposition.entrySet()){
            i += entry.getValue() / this.totalPercentage;
            probability.put(entry.getKey(),i);
        }
        return probability;
    }

    private Map<String,Double> parseString(){
        Map<String,Double> stringDoubleMap = new HashMap<>();
        for (Map.Entry<XMaterial,Double> entry : this.composition.entrySet()){
            stringDoubleMap.put(entry.getKey().toString(),entry.getValue());
        }
        return stringDoubleMap;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String,Object> me = new HashMap<>();
        me.put("blocks", parseString());
        return me;
    }
}
