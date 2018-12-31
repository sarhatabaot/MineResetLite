package com.koletar.jj.mineresetlite.debugger;

import com.koletar.jj.mineresetlite.mine.Mine;

public class MineDebugger {

    public static String toString(Mine mine) {
        String strMine ="name: "+mine.getName()+
                ",world: "+mine.getWorld()+
                ",minPos: "+mine.getMinPos().toString()+
                ",maxPos: "+mine.getMaxPos().toString()+
                ",composition: "+mine.getComposition().parseString().toString();

        if(mine.getTeleportPosition()!=null){
            strMine += ",tpPos: "+mine.getTeleportPosition().toString();
        }
        return strMine;
    }

}
