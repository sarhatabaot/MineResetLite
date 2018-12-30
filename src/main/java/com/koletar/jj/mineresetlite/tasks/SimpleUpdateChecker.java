package com.koletar.jj.mineresetlite.tasks;

import com.koletar.jj.mineresetlite.MineResetLite;
import org.json.JSONObject;

import java.net.URL;
import java.util.Scanner;

public class SimpleUpdateChecker implements Runnable {
    private MineResetLite plugin;
    private String versionNumber;

    public SimpleUpdateChecker(MineResetLite plugin) {
        this.plugin = plugin;
        this.versionNumber = plugin.getDescription().getVersion();
    }

    @Override
    public void run() {
        try {
            URL updateTag = new URL("https://api.github.com/repos/sarhatabaot/MineResetLite/releases/latest");
            Scanner scanner = new Scanner(updateTag.openStream());
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNext()){
                stringBuilder.append(scanner.nextLine());
            }
            scanner.close();
            String str = stringBuilder.toString();

            JSONObject obj = new JSONObject(str);
            String remoteVer = obj.get("tag_name").toString();
            int remoteVal = Integer.valueOf(remoteVer.replace(".", ""));
            int localVer = Integer.valueOf(versionNumber.replace(".", ""));
            if (remoteVal > localVer) {
                plugin.setNeedsUpdate(true);
                plugin.setNewVersion(remoteVer);
                plugin.getLogger().info("New update: "+remoteVer+" Current version: "+versionNumber);
            }
            else {
                plugin.getLogger().info("You are running the latest version: "+versionNumber);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
