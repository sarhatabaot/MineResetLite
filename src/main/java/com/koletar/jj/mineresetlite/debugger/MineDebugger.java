package com.koletar.jj.mineresetlite.debugger;

import com.koletar.jj.mineresetlite.mine.Mine;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class MineDebugger {

    public static String toString(Mine mine) {
        String strMine ="name: "+mine.getName()+
                ",world: "+mine.getWorld()+
                ",minPos: "+mine.getMinPos().toString()+
                ",maxPos: "+mine.getMaxPos().toString()+
                ",composition: "+toStringComposition(mine.getComposition());

        if(mine.getTeleportPosition()!=null){
            strMine += ",tpPos: "+mine.getTeleportPosition().toString();
        }
        return strMine;
    }
    public static String toStringComposition(Map<Material,Double> composition){
        Map<String,Double> sComposition = new HashMap<>();
        for(Map.Entry<Material,Double> entry: composition.entrySet()) {
            sComposition.put(entry.getKey().toString(),entry.getValue());
        }
        return sComposition.toString();
    }

}
