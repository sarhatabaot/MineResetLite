package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Composition implements ConfigurationSerializable {
    private Map<XMaterial,Double> compositionMap;
    private double totalPercentage;
    private XMaterial surface;

    public Composition(){
        this.compositionMap = new HashMap<>();
    }

    public static Composition deserialize(Map<String,Object> me){
        XMaterial surface = null;
        if(me.containsKey("surface") && !me.get("surface").equals("")) {
            surface = XMaterial.fromString((String) me.get("surface"));
        }
        Map<String,Double> stringDoubleMap = (Map<String, Double>) me.get("blocks");
        Map<XMaterial,Double> comp = new HashMap<>();
        for(Map.Entry<String,Double> entry: stringDoubleMap.entrySet()){
            comp.put(XMaterial.fromString(entry.getKey()),entry.getValue());
        }
        return new Composition(comp,surface);
    }

    public Composition(Map<XMaterial, Double> compositionMap, XMaterial surface) {
        this.compositionMap = compositionMap;
        this.surface = surface;
    }

    public Composition(Map<XMaterial, Double> composition) {
        this.compositionMap = composition;
    }

    public Map<XMaterial,Double> getProbability(){
        this.totalPercentage = calcPercentage();
        return padComposition();
    }

    public XMaterial getSurface() {
        return surface;
    }

    public void setSurface(XMaterial surface) {
        this.surface = surface;
    }

    /**
     * Add/replace material in composition map
     * @param material  XMaterial material
     * @param percent   percent
     */
    public void set(XMaterial material, double percent){
        this.compositionMap.put(material,percent);
    }

    public void remove(XMaterial material){
        this.compositionMap.remove(material);
    }

    public Map<XMaterial, Double> getMap() {
        return compositionMap;
    }

    public void setTotalPercentage(double totalPercentage) {
        this.totalPercentage = totalPercentage;
    }

    public double getTotalPercentage() {
        return totalPercentage;
    }

    public double calcPercentage(){
        double percentage = 0;
        for(Map.Entry<XMaterial,Double> entry: compositionMap.entrySet()){
            percentage += entry.getValue();
        }
        return percentage;
    }

    public double calcPercentage(XMaterial material, double newPercentage){
        double percentage = 0;
        for(Map.Entry<XMaterial,Double> entry: compositionMap.entrySet()){
            if(entry.getKey().equals(material)){
                percentage += newPercentage;
                continue;
            }
            percentage += entry.getValue();
        }
        return percentage;
    }

    private Map<XMaterial,Double> padComposition(){
        Map<XMaterial,Double> composition = new HashMap<>(this.compositionMap);
        double airPercentage = 1 - totalPercentage;
        DecimalFormat df = new DecimalFormat("#.##");
        airPercentage = Double.valueOf(df.format(airPercentage));
        if(totalPercentage < 1){
            composition.put(XMaterial.AIR, airPercentage);
            this.totalPercentage = 1;
        }
        return composition;
    }

    public Map<String,Double> parseString(){
        Map<String,Double> stringDoubleMap = new HashMap<>();
        for (Map.Entry<XMaterial,Double> entry : this.compositionMap.entrySet()){
            stringDoubleMap.put(entry.getKey().toString(),entry.getValue());
        }
        return stringDoubleMap;
    }

    public boolean isMaterial(XMaterial material){
        for (Map.Entry<XMaterial,Double> entry: compositionMap.entrySet()){
            if(entry.getKey().equals(material))
                return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String,Object> me = new HashMap<>();
        if(surface!=null) {
            me.put("surface", this.surface.toString());
        }
        else {
            me.put("surface","");
        }
        me.put("blocks", parseString());
        return me;
    }

    public boolean equals(Composition composition){
        return this.compositionMap.equals(composition.getMap());
    }
}
