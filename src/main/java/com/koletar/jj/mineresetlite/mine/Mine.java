package com.koletar.jj.mineresetlite.mine;

import com.koletar.jj.mineresetlite.MineResetLite;
import com.koletar.jj.mineresetlite.events.MineUpdatedEvent;
import com.koletar.jj.mineresetlite.util.Phrases;
import com.koletar.jj.mineresetlite.util.StringTools;
import com.koletar.jj.mineresetlite.util.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author jjkoletar, sarhatabaot
 */
@SerializableAs("Mine")
public class Mine implements ConfigurationSerializable {
    private String name;
    private World world;
    private Position minPos;
    private Position maxPos;

    private XMaterial surface;
    private Composition composition;

    private boolean fillMode;

    //private Reset reset;

    private int resetDelay;
    private List<Integer> resetWarnings;
    private int resetClock;
    private boolean isSilent;

    private TeleportPosition teleportPosition;

    // from MineResetLitePlus
    // should be broken off into its own class.
    private double resetPercent = -1.0;
    private transient int maxCount = 0;
    private transient int currentBroken = 0;

    private List<PotionEffect> potions = new ArrayList<>();

    public Mine(Position minPos, Position maxPos, String name, World world) {
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.name = name;
        this.world = world;

        composition = new Composition();
        resetWarnings = new LinkedList<>();
        setMaxCount();
    }

    /**
     * Deserialize
     */
    @SuppressWarnings("unchecked")
    public Mine(Map<String, Object> me) {
        try {
        	this.minPos = Position.deserialize((Map<String,Object>) me.get("minPos"));
        	this.maxPos = Position.deserialize((Map<String,Object>) me.get("maxPos"));

            setMaxCount();
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error deserializing coordinate pairs");
        }
        try {
            this.world = Bukkit.getServer().getWorld((String) me.get("world"));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error finding world");
        }
        if (this.world == null) {
            Logger logger = Bukkit.getLogger();
            logger.severe("[MineResetLite] Unable to find a world! Please include these logger lines along with the stack trace when reporting this bug!");
            logger.severe("[MineResetLite] Attempted to load world named: " + me.get("world"));
            logger.severe("[MineResetLite] Worlds listed: " + StringTools.buildList(Bukkit.getWorlds(), "", ", "));
            throw new IllegalArgumentException("World was null!");
        }
        try {
            this.composition = Composition.deserialize((Map<String,Object>) me.get("composition"));
        } catch (Throwable t) {
            throw new IllegalArgumentException("Error deserializing composition");
        }
        name = (String) me.get("name");
        resetDelay = (Integer) me.get("resetDelay");
        List<String> warnings = (List<String>) me.get("resetWarnings");
        resetWarnings = new LinkedList<>();
        for (String warning : warnings) {
            try {
                resetWarnings.add(Integer.valueOf(warning));
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Non-numeric reset warnings supplied");
            }
        }
        if (me.containsKey("surface") && !me.get("surface").equals("")) {
            surface = XMaterial.fromString((String) me.get("surface"));
        }
        if (me.containsKey("fillMode")) {
            fillMode = (Boolean) me.get("fillMode");
        }
        if (me.containsKey("resetClock")) {
            resetClock = (Integer) me.get("resetClock");
        }
        //Compat for the clock
        if (resetDelay > 0 && resetClock == 0) {
            resetClock = resetDelay;
        }
        if (me.containsKey("isSilent")) {
            isSilent = (Boolean) me.get("isSilent");
        }
        if (me.containsKey("tpPos")) {
            teleportPosition = TeleportPosition.deserialize((Map<String,Object>) me.get("tpPos"));
        }

        if (me.containsKey("resetPercent")) {
            resetPercent = (double) me.get("resetPercent");
        }

        if (me.containsKey("potions")) {
            potions = new ArrayList<>();
            Map<String, Integer> potionPairs = (Map<String, Integer>) me.get("potions");
            for (Map.Entry<String, Integer> entry : potionPairs.entrySet()) {
                String name = entry.getKey();
                int amp = entry.getValue();
                PotionEffect pot = new PotionEffect(
                        PotionEffectType.getByName(name),
                        Integer.MAX_VALUE,
                        amp);
                potions.add(pot);
            }
        }
    }

    public Position getMinPos() {
        return this.minPos;
    }

    public Position getMaxPos() {
        return this.maxPos;
    }

    public void setMinPos(Position minPos) {
        this.minPos = minPos;
    }

    public void setMaxPos(Position maxPos) {
        this.maxPos = maxPos;
    }

    public TeleportPosition getTeleportPosition() {
        return this.teleportPosition;
    }

    public void setTeleportPosition(TeleportPosition teleportPosition) {
        this.teleportPosition = teleportPosition;
    }


    public Map<String, Object> serialize() {
        Map<String, Object> me = new HashMap<>();

        me.put("world", this.world.getName());
        me.put("name", this.name);
        me.put("maxPos", this.maxPos.serialize());
        me.put("minPos", this.minPos.serialize());

        if (this.surface != null) {
            me.put("surface", this.surface.toString());
        } else {
            me.put("surface", "");
        }
        me.put("composition", this.composition.serialize());

        me.put("fillMode", this.fillMode);

        me.put("resetDelay", this.resetDelay);
        me.put("resetClock", this.resetClock);
        List<String> warnings = new LinkedList<>();
        for (Integer warning : this.resetWarnings) {
            warnings.add(warning.toString());
        }
        me.put("resetWarnings", warnings);
        me.put("isSilent", this.isSilent);

        me.put("resetPercent", this.resetPercent);

        if (this.teleportPosition != null) {
            me.put("tpPos", this.teleportPosition.serialize());
        }

        Map<String, Integer> potionPairs = new HashMap<>();
        for (PotionEffect pe : this.potions) {
            potionPairs.put(pe.getType().getName(), pe.getAmplifier());
        }
        me.put("potions", potionPairs);

        return me;
    }

    public boolean getFillMode() {
        return fillMode;
    }

    public void setFillMode(boolean fillMode) {
        this.fillMode = fillMode;
    }

    public void setResetDelay(int minutes) {
        this.resetDelay = minutes;
        this.resetClock = minutes;
    }

    public void setResetWarnings(List<Integer> warnings) {
        resetWarnings = warnings;
    }

    public List<Integer> getResetWarnings() {
        return resetWarnings;
    }

    public int getResetDelay() {
        return resetDelay;
    }

    /**
     * Return the length of time until the next automatic reset.
     * The actual length of time is anywhere between n and n-1 minutes.
     *
     * @return clock ticks left until reset
     */
    public int getTimeUntilReset() {
        return resetClock;
    }

    public XMaterial getSurface() {
        return surface;
    }

    public void setSurface(XMaterial surface) {
        this.surface = surface;
    }

    public World getWorld() {
        return world;
    }

    public String getName() {
        return name;
    }

    public Composition getComposition() {
        return composition;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilence(boolean isSilent) {
        this.isSilent = isSilent;
    }

    public double getCompositionTotal() {
        return composition.getTotalPercentage();
    }

    public boolean isInside(Player p) {
        return isInside(p.getLocation());
    }

    public boolean isInside(Location location) {
        return location.getWorld().equals(world) && inRange(location);
    }

    private boolean inRange(Location location){
        return inRangeX(location.getBlockX()) && inRangeY(location.getBlockY()) && inRangeZ(location.getBlockZ());
    }
    private boolean inRangeX(int x){
        return (x >= minPos.getX() && x <= maxPos.getX());
    }
    private boolean inRangeY(int y){
        return (y >= minPos.getY() && y <= maxPos.getY());
    }
    private boolean inRangeZ(int z){
        return (z >= minPos.getZ() && z <= maxPos.getZ());
    }

    public void setTp(Location location) {
        this.teleportPosition = new TeleportPosition(location);
    }

    private Location getLocationTp() {
        return teleportPosition.toLocation(world);
    }

    public void teleport(Player player) {
        Location location;

        if (!getLocationTp().equals(new Location(world, 0, -Integer.MAX_VALUE, 0))) {
            location = getLocationTp();
        } else {
            Location max = maxPos.toLocation(world);
            Location min = minPos.toLocation(world);
            location = max.add(min).multiply(0.5); // why?
            Block block = location.getBlock();

            if (isUnSafe(block)) {
                location = generateSafeLocation(location);
            }
        }

        player.teleport(location);
    }
    private Location generateSafeLocation(Location location){
        return new Location(world,location.getX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getZ());
    }

    private boolean isUnSafe(Block block){
        return block.getType() != Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR;
    }

    /**
     * @param playerLocation The players location
     * @return location - the safe location.
     */
    @NotNull
    private Location getSafeLocation(Location playerLocation) {
        Location location = new Location(world, playerLocation.getX(), maxPos.getY() + 1D, playerLocation.getZ());
        Block block = location.getBlock();

        // check to make sure we don't suffocate player (TODO: Appears in teleport(Player p) before this is run)
        if (isUnSafe(block)) {
            location = generateSafeLocation(location);
        }
        return location;
    }

    /**
     * Teleports players to a safe location
     */
    private void teleportPlayers() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Location playerLocation = player.getLocation();
            if (isInside(player)) {
                if (teleportPosition!=null) {
                    player.teleport(getLocationTp());
                } else {
                    Location safeLocation = getSafeLocation(playerLocation);
                    player.teleport(safeLocation);
                }
            }
        }
    }

    /**
     * Resets the mine using the <code>composition</code>.
     */
    public void reset() {
        Map<XMaterial, Double> probabilityMap = composition.getProbability();
        teleportPlayers();
        //Actually reset
        Random rand = new Random();
        for (int x = minPos.getX(); x <= maxPos.getX(); ++x) {
            for (int y = minPos.getY(); y <= maxPos.getY(); ++y) {
                for (int z = minPos.getZ(); z <= maxPos.getZ(); ++z) {
                    if (!fillMode || world.getBlockAt(x, y, z).getType() == Material.AIR) {
                        setSurfaceBlocks(x,y,z);
                        generateRandomBlock(rand,probabilityMap,x,y,z);
                    }
                }
            }
        }
		this.currentBroken = 0;
    }

    private void generateRandomBlock(Random random, Map<XMaterial,Double> probabilityMap, int x,int y,int z){
        double r = random.nextDouble();
        for (Map.Entry<XMaterial, Double> entry : probabilityMap.entrySet()) {
            if (r <= entry.getValue()) {
                Block b = world.getBlockAt(x, y, z);
                b.setType(entry.getKey().parseMaterial());
                b.setData((byte)XMaterial.fromString(entry.getKey().toString()).getData());
                break;
            }
        }
    }
    /**
     * Sets the surface blocks, if surface is set.
     * @param x     x
     * @param y     y
     * @param z     z
     */
    private void setSurfaceBlocks(int x,int y, int z){
        if(y== maxPos.getY()&& surface !=null){
            Block block = world.getBlockAt(x,y,z);
            block.setType(surface.parseMaterial());
            block.setData((byte)surface.getData());
        }
    }

    public void cron() {
        if (resetDelay == 0) {
            return;
        }
        if (resetClock > 0) {
            resetClock--; //Tick down to the reset
        }
        if (resetClock == 0) {
            if (!isSilent) {
                MineResetLite.broadcast(Phrases.phrase("mineAutoResetBroadcast", this), this);
            }
            reset();
            resetClock = resetDelay;
            return;
        }
        for (Integer warning : resetWarnings) {
            if (warning == resetClock) {
                MineResetLite.broadcast(Phrases.phrase("mineWarningBroadcast", this, warning), this);
            }
        }
    }

    private void setMaxCount() {
        int dx = maxPos.getX() - minPos.getX() + 1;
        int dy = maxPos.getY() - minPos.getY() + 1;
        int dz = maxPos.getZ() - minPos.getZ() + 1;

        this.maxCount = dx * dy * dz;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public void setResetPercent(double per) {
        this.resetPercent = per;
    }

    public double getResetPercent() {
        return this.resetPercent;
    }

    public void setBrokenBlocks(int broken) {
        this.currentBroken = broken;
        // send mine changed event
        //mi.updateSigns();
        MineUpdatedEvent mineUpdatedEvent = new MineUpdatedEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(mineUpdatedEvent);

        if (this.resetPercent > 0 && this.currentBroken >= (this.maxCount * (1.0 - this.resetPercent))) {
            reset();
            if (!isSilent)
                MineResetLite.broadcast(Phrases.phrase("mineAutoResetBroadcast", this), this);
        }
    }

    public int getBrokenBlocks() {
        return this.currentBroken;
    }

    public List<PotionEffect> getPotions() {
        return this.potions;
    }

    public void addPotion(String strPotion) {
        String[] tokens = strPotion.split(":");
        int amp = 1;
        try {
            if (tokens.length > 1) {
                amp = Integer.valueOf(tokens[1]);
            }
        } catch (Throwable ignore) {

        }
        removePotion(tokens[0]);
        PotionEffect pot = new PotionEffect(
                PotionEffectType.getByName(tokens[0]),
                Integer.MAX_VALUE,
                amp);
        potions.add(pot);
    }

    public void removePotion(String potion) {
        PotionEffect found = null;
        for (PotionEffect potionEffect : potions) {
            if (potionEffect.getType().getName().equalsIgnoreCase(potion)) {
                found = potionEffect;
                break;
            }
        }
        if (found != null)
            potions.remove(found);
    }
}
