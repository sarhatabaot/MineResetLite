package com.koletar.jj.mineresetlite.placeholders;

import com.koletar.jj.mineresetlite.Mine;
import com.koletar.jj.mineresetlite.MineResetLite;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class MinePlaceholders extends PlaceholderExpansion {
    private MineResetLite mrl = (MineResetLite) Bukkit.getPluginManager().getPlugin("MineResetLite");


    @Override
    public String getIdentifier() {
        return "mrl";
    }

    @Override
    public String getAuthor() {
        return "sarhatabaot";
    }

    @Override
    public String getVersion() {
        return "v1.0.0";
    }

    /* %identifier_mine_argument% */
    @Override
    public String onPlaceholderRequest(Player p, String args) {
        args = args.toLowerCase();
        String[] bits = args.split("_");
        String value = bits[1];
        Mine[] mines = mrl.matchMines(bits[0]);
        String strPlaceholder="";
        switch (value){
            case "time":{
                strPlaceholder =  String.valueOf(mines[0].getResetDelay());
                break;
            }
            case "timeremaining":{
                strPlaceholder = String.valueOf(mines[0].getTimeUntilReset());
                break;
            }
            case "name":{
                strPlaceholder = mines[0].getName();
                break;
            }
            case "world":{
                strPlaceholder = mines[0].getWorld().getName();
                break;
            }
            case "percentage":{
                strPlaceholder = String.valueOf(mines[0].getResetPercent());
                break;
            }
            case "blocks_mined":{
                strPlaceholder = String.valueOf(mines[0].getBrokenBlocks());
                break;
            }
            case "percentage_mined":{
                strPlaceholder = String.valueOf((mines[0].getBrokenBlocks()/mines[0].getMaxCount())*100);
                break;
            }
        }
        return strPlaceholder;
    }

}
