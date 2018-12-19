package com.koletar.jj.mineresetlite;

import com.koletar.jj.mineresetlite.events.MineUpdatedEvent;
import com.koletar.jj.mineresetlite.util.Phrases;
import com.koletar.jj.mineresetlite.util.StringTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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
 * @author jjkoletar
 */
public class Mine implements ConfigurationSerializable {
	private String name;
	private World world;
	private Position minPos;
	private Position maxPos;

	private SerializableBlock surface;
	private Map<SerializableBlock, Double> composition;

	private boolean fillMode;

	private Reset reset;

	private int resetDelay;
	private List<Integer> resetWarnings;
	private int resetClock;
	private boolean isSilent;

	private TeleportPosition teleportPosition;
	
	// from MineResetLitePlus
	private double resetPercent = -1.0;
	private transient int maxCount = 0;
	private transient int currentBroken = 0;
	
	private List<PotionEffect> potions = new ArrayList<>();

	public Mine(Position minPos,Position maxPos, String name, World world){
		this.minPos = minPos;
		this.maxPos = maxPos;
		this.name = name;
		this.world = world;

		composition = new HashMap<>();
		resetWarnings = new LinkedList<>();
		setMaxCount();
	}

	/**
	 * Deserialize
	 */
	public Mine(Map<String, Object> me) {
		try {
			this.minPos = (Position) me.get("minPos");
			this.maxPos = (Position) me.get("maxPos");

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
			Map<String, Double> sComposition = (Map<String, Double>) me.get("composition");
			this.composition = new HashMap<>();
			for (Map.Entry<String, Double> entry : sComposition.entrySet()) {
				this.composition.put(new SerializableBlock(entry.getKey()), entry.getValue());
			}
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
			surface = new SerializableBlock((String) me.get("surface"));
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
			teleportPosition = (TeleportPosition) me.get("tpPos");
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

	public Position getMinPos(){
		return this.minPos;
	}
	public Position getMaxPos(){
		return this.maxPos;
	}
	public void setMinPos(Position minPos){
		this.minPos = minPos;
	}
	public void setMaxPos(Position maxPos){
		this.maxPos = maxPos;
	}
	public TeleportPosition getTeleportPosition(){
		return this.teleportPosition;
	}
	public void setTeleportPosition(TeleportPosition teleportPosition){
		this.teleportPosition = teleportPosition;
	}

	
	public Map<String, Object> serialize() {
		Map<String, Object> me = new HashMap<>();

		me.put("name", this.name);
		me.put("maxPos",this.maxPos.serialize());
		me.put("minPos",this.minPos.serialize());
		me.put("world", this.world.getName());

		if (this.surface != null) {
			me.put("surface", this.surface.toString());
		} else {
			me.put("surface", "");
		}
		//Make string form of composition
		Map<String, Double> sComposition = new HashMap<>();
		for (Map.Entry<SerializableBlock, Double> entry : this.composition.entrySet()) {
			sComposition.put(entry.getKey().toString(), entry.getValue());
		}
		me.put("composition", sComposition);

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

		me.put("tpPos",this.teleportPosition.serialize());

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
	
	public SerializableBlock getSurface() {
		return surface;
	}
	
	public void setSurface(SerializableBlock surface) {
		this.surface = surface;
	}
	
	public World getWorld() {
		return world;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<SerializableBlock, Double> getComposition() {
		return composition;
	}
	
	public boolean isSilent() {
		return isSilent;
	}
	
	public void setSilence(boolean isSilent) {
		this.isSilent = isSilent;
	}
	
	public double getCompositionTotal() {
		double total = 0;
		for (Double d : composition.values()) {
			total += d;
		}
		return total;
	}
	
	public boolean isInside(Player p) {
		return isInside(p.getLocation());
	}
	
	public boolean isInside(Location l) {
		return l.getWorld().equals(world)
				&& (l.getBlockX() >= minPos.getX() && l.getBlockX() <= maxPos.getX())
				&& (l.getBlockY() >= minPos.getY() && l.getBlockY() <= maxPos.getY())
				&& (l.getBlockZ() >= minPos.getZ() && l.getBlockZ() <= maxPos.getZ());
	}
	
	public void setTp(Location l) {
		this.teleportPosition = new TeleportPosition(l.getBlockX(),
				l.getBlockY(),
				l.getBlockZ(),
				(int)l.getPitch(),
				(int)l.getYaw());
	}
	
	private Location getTp() {
		return new Location(getWorld(),
				teleportPosition.getX(),
				teleportPosition.getY(),
				teleportPosition.getZ(),
				teleportPosition.getYaw(),
				teleportPosition.getPitch());
	}

	@NotNull
	private Location getSafeLocation(Location playerLocation){
		Location location = new Location(world, playerLocation.getX(), maxPos.getY() + 1D, playerLocation.getZ());
		Block block = location.getBlock();

		// check to make sure we don't suffocate player
		if (block.getType() != Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) {
			location = new Location(world, playerLocation.getX(),
					playerLocation.getWorld().getHighestBlockYAt(playerLocation.getBlockX(),
							playerLocation.getBlockZ()),
					playerLocation.getZ());
		}
		return location;
	}
	private void teleportPlayers(){
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Location playerLocation = player.getLocation();
			if (isInside(player)) {
				if (teleportPosition.getY() > -Integer.MAX_VALUE) {
					player.teleport(getTp());
				} else {
					Location safeLocation = getSafeLocation(playerLocation);
					player.teleport(safeLocation);
				}
			}
		}
	}
	public void reset() {
		//Get probability map
		List<CompositionEntry> probabilityMap = mapComposition(composition);
		//Pull players out
		teleportPlayers();
		//Actually reset
		Random rand = new Random();
		for (int x = minPos.getX(); x <= maxPos.getX(); ++x) {
			for (int y = minPos.getY(); y <= maxPos.getY(); ++y) {
				for (int z = minPos.getZ(); z <= maxPos.getZ(); ++z) {
					if (!fillMode || world.getBlockAt(x, y, z).getType() == Material.AIR) {
						// set surface
						if (y == maxPos.getY() && surface != null) {
							//world.getBlockAt(x, y, z).setTypeIdAndData(surface.getBlockId(), surface.getData(), false);
							Block block = world.getBlockAt(x, y, z);
							block.setType(surface.getBlockType());
							if (surface.getData() > 0) {
								try {
									//ReflectionUtil.makePerform(block, "setData", new Object[]{surface.getData()});
								} catch (Throwable ignore) {
								
								}
							}
							continue;
						}
						double r = rand.nextDouble();
						for (CompositionEntry ce : probabilityMap) {
							if (r <= ce.getChance()) {
								//world.getBlockAt(x, y, z).setTypeIdAndData(ce.getBlock().getBlockId(), ce.getBlock().getData(), false);
								Block b = world.getBlockAt(x, y, z);
								b.setType(ce.getBlock().getBlockType());
								if (ce.getBlock().getData() > 0) {
									try {
										//ReflectionUtil.makePerform(b, "setData", new Object[]{ce.getBlock().getData()});
									} catch (Throwable ignore) {
									
									}
								}
								break;
							}
						}
					}
				}
			}
		}
		resetBrokenBlocks();
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
	
	public static class CompositionEntry {
		private SerializableBlock block;
		private double chance;
		
		public CompositionEntry(SerializableBlock block, double chance) {
			this.block = block;
			this.chance = chance;
		}
		
		public SerializableBlock getBlock() {
			return block;
		}
		
		double getChance() {
			return chance;
		}
	}
	
	private static ArrayList<CompositionEntry> mapComposition(Map<SerializableBlock, Double> compositionIn) {
		ArrayList<CompositionEntry> probabilityMap = new ArrayList<>();
		Map<SerializableBlock, Double> composition = new HashMap<>(compositionIn);
		double max = 0;
		for (Map.Entry<SerializableBlock, Double> entry : composition.entrySet()) {
			max += entry.getValue();
		}
		//Pad the remaining percentages with air
		if (max < 1) {
			composition.put(new SerializableBlock(0), 1 - max);
			max = 1;
		}
		double i = 0;
		for (Map.Entry<SerializableBlock, Double> entry : composition.entrySet()) {
			double v = entry.getValue() / max;
			i += v;
			probabilityMap.add(new CompositionEntry(entry.getKey(), i));
		}
		return probabilityMap;
	}
	
	public void teleport(Player player) {
		Location location;
		
		if (!getTp().equals(new Location(world, 0, -Integer.MAX_VALUE, 0))) {
			location = getTp();
		} else {
			Location max = new Location(world,
					Math.max(this.maxPos.getX(), this.minPos.getX()), this.maxPos.getY(), Math.max(this.maxPos.getZ(), this.minPos.getZ()));
			Location min = new Location(world,
					Math.min(this.maxPos.getX(), this.minPos.getX()), this.minPos.getY(), Math.min(this.maxPos.getZ(), this.minPos.getZ()));
			
			location = max.add(min).multiply(0.5);
			Block block = location.getBlock();
			
			if (block.getType() != Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) {
				location = new Location(world, location.getX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getZ());
			}
		}
		
		player.teleport(location);
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
	
	private void resetBrokenBlocks() {
		this.currentBroken = 0;
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
