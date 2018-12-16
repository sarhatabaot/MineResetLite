package com.koletar.jj.mineresetlite;

import com.vk2gpz.mineresetlite.event.MineUpdatedEvent;
import com.vk2gpz.vklib.reflection.ReflectionUtil;
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
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;
	private World world;
	private Map<SerializableBlock, Double> composition;
	private int resetDelay;
	private List<Integer> resetWarnings;
	private String name;
	private SerializableBlock surface;
	private boolean fillMode;
	private int resetClock;
	private boolean isSilent;
	private int tpX = 0;
	private int tpY = -Integer.MAX_VALUE;
	private int tpZ = 0;
	private int tpYaw = 0;
	private int tpPitch = 0;
	
	// from MineResetLitePlus
	private double resetPercent = -1.0;
	private transient int maxCount = 0;
	private transient int currentBroken = 0;
	
	private List<PotionEffect> potions = new ArrayList<>();

	public Mine(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, String name, World world) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.name = name;
		this.world = world;
		composition = new HashMap<>();
		resetWarnings = new LinkedList<>();
		
		setMaxCount();
	}
	
	public Mine(Map<String, Object> me) {
		try {
			minX = (Integer) me.get("minX");
			minY = (Integer) me.get("minY");
			minZ = (Integer) me.get("minZ");
			maxX = (Integer) me.get("maxX");
			maxY = (Integer) me.get("maxY");
			maxZ = (Integer) me.get("maxZ");
			
			setMaxCount();
		} catch (Throwable t) {
			throw new IllegalArgumentException("Error deserializing coordinate pairs");
		}
		try {
			world = Bukkit.getServer().getWorld((String) me.get("world"));
		} catch (Throwable t) {
			throw new IllegalArgumentException("Error finding world");
		}
		if (world == null) {
			Logger logger = Bukkit.getLogger();
			logger.severe("[MineResetLite] Unable to find a world! Please include these logger lines along with the stack trace when reporting this bug!");
			logger.severe("[MineResetLite] Attempted to load world named: " + me.get("world"));
			logger.severe("[MineResetLite] Worlds listed: " + StringTools.buildList(Bukkit.getWorlds(), "", ", "));
			throw new IllegalArgumentException("World was null!");
		}
		try {
			Map<String, Double> sComposition = (Map<String, Double>) me.get("composition");
			composition = new HashMap<>();
			for (Map.Entry<String, Double> entry : sComposition.entrySet()) {
				composition.put(new SerializableBlock(entry.getKey()), entry.getValue());
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
		if (me.containsKey("surface")) {
			if (!me.get("surface").equals("")) {
				surface = new SerializableBlock((String) me.get("surface"));
			}
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
		if (me.containsKey("tpY")) { // Should contain all three if it contains this one
			tpX = (int) me.get("tpX");
			tpY = (int) me.get("tpY");
			tpZ = (int) me.get("tpZ");
		}
		
		if (me.containsKey("tpYaw")) {
			tpYaw = (int) me.get("tpYaw");
			tpPitch = (int) me.get("tpPitch");
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
	
	public Map<String, Object> serialize() {
		Map<String, Object> me = new HashMap<>();
		me.put("minX", minX);
		me.put("minY", minY);
		me.put("minZ", minZ);
		me.put("maxX", maxX);
		me.put("maxY", maxY);
		me.put("maxZ", maxZ);
		me.put("world", world.getName());
		//Make string form of composition
		Map<String, Double> sComposition = new HashMap<>();
		for (Map.Entry<SerializableBlock, Double> entry : composition.entrySet()) {
			sComposition.put(entry.getKey().toString(), entry.getValue());
		}
		me.put("composition", sComposition);
		me.put("name", name);
		me.put("resetDelay", resetDelay);
		List<String> warnings = new LinkedList<>();
		for (Integer warning : resetWarnings) {
			warnings.add(warning.toString());
		}
		me.put("resetWarnings", warnings);
		if (surface != null) {
			me.put("surface", surface.toString());
		} else {
			me.put("surface", "");
		}
		me.put("fillMode", fillMode);
		me.put("resetClock", resetClock);
		me.put("isSilent", isSilent);
		me.put("tpX", tpX);
		me.put("tpY", tpY);
		me.put("tpZ", tpZ);
		me.put("tpYaw", tpYaw);
		me.put("tpPitch", tpPitch);
		
		me.put("resetPercent", resetPercent);
		
		Map<String, Integer> potionpairs = new HashMap<>();
		for (PotionEffect pe : this.potions) {
			potionpairs.put(pe.getType().getName(), pe.getAmplifier());
		}
		me.put("potions", potionpairs);
		
		return me;
	}
	
	public boolean getFillMode() {
		return fillMode;
	}
	
	public void setFillMode(boolean fillMode) {
		this.fillMode = fillMode;
	}
	
	public void setResetDelay(int minutes) {
		resetDelay = minutes;
		resetClock = minutes;
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
				&& (l.getBlockX() >= minX && l.getBlockX() <= maxX)
				&& (l.getBlockY() >= minY && l.getBlockY() <= maxY)
				&& (l.getBlockZ() >= minZ && l.getBlockZ() <= maxZ);
	}
	
	public void setTp(Location l) {
		tpX = l.getBlockX();
		tpY = l.getBlockY();
		tpZ = l.getBlockZ();
		tpYaw = (int) l.getYaw();
		tpPitch = (int) l.getPitch();
	}
	
	private Location getTp() {
		return new Location(getWorld(), tpX, tpY, tpZ, tpYaw, tpPitch);
	}
	
	public void reset() {
		//Get probability map
		List<CompositionEntry> probabilityMap = mapComposition(composition);
		//Pull players out
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Location playerLocation = player.getLocation();
			if (isInside(player)) {
				//p.teleport(new Location(world, l.getX(), maxY + 2D, l.getZ()));
				if (tpY > -Integer.MAX_VALUE) {
					player.teleport(getTp());
				} else { // empty spawn location!
					// find the safe landing location!
					Location tp = new Location(world, playerLocation.getX(), maxY + 1D, playerLocation.getZ());
					Block block = tp.getBlock();
					
					// check to make sure we don't suffocate player
					if (block.getType() != Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) {
						tp = new Location(world, playerLocation.getX(),
								playerLocation.getWorld().getHighestBlockYAt(playerLocation.getBlockX(),
								playerLocation.getBlockZ()),
								playerLocation.getZ());
					}
					player.teleport(tp);
				}
			}
		}
		//Actually reset
		Random rand = new Random();
		for (int x = minX; x <= maxX; ++x) {
			for (int y = minY; y <= maxY; ++y) {
				for (int z = minZ; z <= maxZ; ++z) {
					if (!fillMode || world.getBlockAt(x, y, z).getType() == Material.AIR) {
						if (y == maxY && surface != null) {
							//world.getBlockAt(x, y, z).setTypeIdAndData(surface.getBlockId(), surface.getData(), false);
							Block b = world.getBlockAt(x, y, z);
							b.setType(surface.getBlockType());
							if (surface.getData() > 0) {
								try {
									ReflectionUtil.makePerform(b, "setData", new Object[]{surface.getData()});
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
										ReflectionUtil.makePerform(b, "setData", new Object[]{ce.getBlock().getData()});
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
		resetMRLP();
	}
	
	void cron() {
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
			Location max = new Location(world, Math.max(this.maxX, this.minX), this.maxY, Math.max(this.maxZ, this.minZ));
			Location min = new Location(world, Math.min(this.maxX, this.minX), this.minY, Math.min(this.maxZ, this.minZ));
			
			location = max.add(min).multiply(0.5);
			Block block = location.getBlock();
			
			if (block.getType() != Material.AIR || block.getRelative(BlockFace.UP).getType() != Material.AIR) {
				location = new Location(world, location.getX(), location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()), location.getZ());
			}
		}
		
		player.teleport(location);
	}
	
	private void setMaxCount() {
		int dx = maxX - minX + 1;
		int dy = maxY - minY + 1;
		int dz = maxZ - minZ + 1;
		
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
	
	private void resetMRLP() {
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
