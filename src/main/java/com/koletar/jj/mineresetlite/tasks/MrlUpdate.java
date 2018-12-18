package com.koletar.jj.mineresetlite.tasks;

import com.koletar.jj.mineresetlite.MineResetLite;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MrlUpdate implements Runnable {
    private MineResetLite plugin;
    private String versionNumber;

    public MrlUpdate(MineResetLite plugin) {
        this.plugin = plugin;
        this.versionNumber = plugin.getDescription().getVersion();
    }

    @Override
    public void run() {
        try {
            URL updateFile = new URL("https://api.github.com/repos/sarhatabaot/MineResetLite/releases/latest");
            URLConnection conn = updateFile.openConnection();
            String rv = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();
            JSONArray resp = (JSONArray) JSONValue.parse(rv); //JSONObject cannot be cast to JSONArray
            if (resp.size() == 0)
                return;
            String name = ((JSONObject) resp.get(resp.size() - 1)).get("tag_name").toString();
            String[] bits = name.split(" ");
            String remoteVer = bits[bits.length - 1];
            int remoteVal = Integer.valueOf(remoteVer.replace(".", ""));
            int localVer = Integer.valueOf(versionNumber.replace(".", ""));
            if (remoteVal > localVer) {
                plugin.setNeedsUpdate(true);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
