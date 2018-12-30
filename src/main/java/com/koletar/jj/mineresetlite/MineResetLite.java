package com.koletar.jj.mineresetlite;

import com.koletar.jj.mineresetlite.command.CommandManager;
import com.koletar.jj.mineresetlite.command.commands.MineCommands;
import com.koletar.jj.mineresetlite.command.commands.PluginCommands;
import com.koletar.jj.mineresetlite.listeners.BrokenBlockEventListener;
import com.koletar.jj.mineresetlite.listeners.PlayerEventListener;
import com.koletar.jj.mineresetlite.mine.Mine;
import com.koletar.jj.mineresetlite.mine.Position;
import com.koletar.jj.mineresetlite.mine.TeleportPosition;
import com.koletar.jj.mineresetlite.placeholders.MinePlaceholders;
import com.koletar.jj.mineresetlite.tasks.SimpleUpdateChecker;
import com.koletar.jj.mineresetlite.util.Phrases;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author jjkoletar, vk2gpz, sarhatabaot
 */
public class MineResetLite extends JavaPlugin {
	public List<Mine> mines;
	private Logger logger;
	private CommandManager commandManager;
	private WorldEditPlugin worldEdit;
	private int saveTaskId = -1;
	private int resetTaskId = -1;
	private BukkitTask updateTask = null;
	private boolean needsUpdate;
	
	private static class IsMineFile implements FilenameFilter {
		public boolean accept(File file, String s) {
			return s.contains(".mine.yml");
		}
	}

	public boolean isNeedsUpdate() {
		return needsUpdate;
	}

	public void onEnable() {
		ConfigurationSerialization.registerClass(Mine.class);
		ConfigurationSerialization.registerClass(Position.class);
		ConfigurationSerialization.registerClass(TeleportPosition.class);

		logger = getLogger();
		if (!setupConfig()) {
			logger.severe("Couldn't setup config files. Plugin loading aborted!");
			return;
		}

		commandManager = new CommandManager();
		commandManager.register(CommandManager.class, commandManager);
		commandManager.register(MineCommands.class, new MineCommands(this));
		commandManager.register(PluginCommands.class, new PluginCommands(this));

		initPhrases();
		initPlugins();
		initMines();
		initTasks();
		registerListeners();

		if(Config.isDebug()) {
			logger.info("Init phrases done.");
			logger.info("Init plugins done.");
			logger.info("Init mines done.");
			logger.info("Init tasks done.");
			logger.info("registered listeners.");
		}
		logger.info("MineResetLite version " + getDescription().getVersion() + " enabled!");
	}

	private void initTasks(){
		if(Config.getCheckForUpdates()){
			updateTask = Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(
					this, new SimpleUpdateChecker(this),20 * 15);
			logger.info("Check for update done.");
		}
		// MineReset Task
		// reset task - every minute
		resetTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Mine mine : mines) {
				mine.cron();
			}
		}, 60 * 20L, 60 * 20L);
	}
	private void initPlugins(){
		if (getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
			this.worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
			logger.info("WorldEdit hooked.");
		}
		if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")){
			new MinePlaceholders().register();
			logger.info("MRL PlaceholderExpansion hooked");
		}

	}
	private void initMines() {
		mines = new ArrayList<>();
		File[] mineFiles = new File(getDataFolder(), "mines").listFiles(new IsMineFile());
		assert mineFiles != null;
		for (File file : mineFiles) {
			logger.info("Loading mine from file '" + file.getName() + "'...");
			FileConfiguration fileConf = YamlConfiguration.loadConfiguration(file);
			try {
				Object o = fileConf.get("mine");
				if (!(o instanceof Mine)) {
					logger.severe("Mine wasn't a mine object! Something is off with serialization!");
					continue;
				}
				Mine mine = (Mine) o;
				mines.add(mine);
			} catch (Throwable t) {
				logger.severe("Unable to load mine!");
			}
		}
	}

	private void initPhrases(){
		Locale locale = new Locale(Config.getLocale());
		Phrases.getInstance().initialize(locale);
		File overrides = new File(getDataFolder(), "phrases.properties");
		if (overrides.exists()) {
			Properties overridesProps = new Properties();
			try {
				overridesProps.load(new FileInputStream(overrides));
			} catch (IOException e) {
				e.printStackTrace();
			}
			Phrases.getInstance().overrides(overridesProps);
		}
	}
	
	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BrokenBlockEventListener(this), this);
		pm.registerEvents(new PlayerEventListener(this), this);
	}

	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}
	
	public void onDisable() {
		getServer().getScheduler().cancelTask(resetTaskId);
		getServer().getScheduler().cancelTask(saveTaskId);
		if (updateTask != null) {
			updateTask.cancel();
		}
		HandlerList.unregisterAll(this);
		//save();
		logger.info("MineResetLite disabled");
	}

	/**
	 *
	 * @param name
	 * @return
	 */
	public Material matchMaterial(String name) {
		Material ret = Material.getMaterial(name.toUpperCase());
		if(ret==null)
			ret = Material.matchMaterial(name);
		return ret;
	}

	/**
	 *
	 * @param in Mine name
	 * @return
	 */
	public Mine[] matchMines(String in) {
		String strMine = in;
		List<Mine> matches = new LinkedList<>();
		boolean wildcard = strMine.contains("*");
		strMine = strMine.replace("*", "").toLowerCase();
		for (Mine mine : mines) {
			if (wildcard) {
				if (mine.getName().toLowerCase().contains(strMine)) {
					matches.add(mine);
				}
			} else {
				if (mine.getName().equalsIgnoreCase(strMine)) {
					matches.add(mine);
				}
			}
		}
		return matches.toArray(new Mine[0]);
	}
	
	public String toString(Mine[] mines) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mines.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			Mine mine = mines[i];
			sb.append(mine.getName());
		}
		return sb.toString();
	}
	
	/**
	 * Alert the plugin that changes have been made to mines, but wait 60 seconds before we save.
	 * This process saves on disk I/O by waiting until a long string of changes have finished before writing to disk.
	 */
	public void buffSave() {
		BukkitScheduler scheduler = getServer().getScheduler();
		if (saveTaskId != -1) {
			//Cancel old task
			scheduler.cancelTask(saveTaskId);
		}
		//Schedule save
		final MineResetLite plugin = this;
		scheduler.scheduleSyncDelayedTask(this, plugin::save, 60 * 20L);
	}

	/**
	 * Saves the mines to a file
	 */
	private void save() {
		for (Mine mine : mines) {
			File mineFile = getMineFile(mine);
			FileConfiguration mineConf = YamlConfiguration.loadConfiguration(mineFile);
			mineConf.set("mine", mine);
			try {
				mineConf.save(mineFile);
			} catch (IOException e) {
				logger.severe("Unable to serialize mine!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets-up the config file
	 * @return returns if config was setup correctly
	 */
	private boolean setupConfig() {
		File pluginFolder = getDataFolder();
		if (!pluginFolder.exists() && !pluginFolder.mkdir()) {
			logger.severe("Could not make plugin folder! This won't end well...");
			return false;
		}
		File mineFolder = new File(getDataFolder(), "mines");
		if (!mineFolder.exists() && !mineFolder.mkdir()) {
			logger.severe("Could not make mine folder! Abort! Abort!");
			return false;
		}
		try {
			Config.initConfig(getDataFolder());
		} catch (IOException e) {
			logger.severe("Could not make config file!");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Base /mrl command, runs the help command if no additional arguments are entered
	 * @param args
	 * @param command
	 * @param label
	 * @param sender type of sender, can be console or player
	 */
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("mineresetlite")) {
			if (args.length == 0) {
				String[] helpArgs = new String[0];
				commandManager.callCommand("help", sender, helpArgs);
				return true;
			}
			//Spoof args array to account for the initial sub-command specification
			String[] spoofedArgs = new String[args.length - 1];
			System.arraycopy(args, 1, spoofedArgs, 0, args.length - 1);
			commandManager.callCommand(args[0], sender, spoofedArgs);
			return true;
		}
		return false; //Fallthrough
	}

	/**
	 * Broadcasts a reset message according to the Config
	 * if nothing is set, broadcasts to everyone.
	 * <p>
	 * <code>broadcastNearby()</code> - broadcasts to player near the mine.
	 * <code>broadcastInWorldOnly()</code> - broadcasts only in the world.
	 * @param message 	message to broadcast
	 * @param mine 		mine that's reset
	 */
	public static void broadcast(String message, Mine mine) {
		if (Config.getBroadcastNearbyOnly()) {
			broadcastNearby(message,mine);
		} else if (Config.getBroadcastInWorldOnly()) {
			broadcastInWorldOnly(message,mine);
		} else {
			Bukkit.getServer().broadcastMessage(message);
		}
	}

	/**
	 * Broadcast a reset message only to players near the mine
	 * @param message 	message to broadcast
	 * @param mine 		mine that's reset
	 */
	private static void broadcastNearby(String message, @NotNull Mine mine){
		for (Player p : mine.getWorld().getPlayers()) {
			if (mine.isInside(p)) {
				p.sendMessage(message);
			}
		}
		Bukkit.getLogger().info(message);
	}

	/**
	 * Broadcast a reset message only in the world
	 * @param message 	message to broadcast
	 * @param mine 		mine that's reset
	 */
	private static void broadcastInWorldOnly(String message, @NotNull Mine mine){
		for (Player p : mine.getWorld().getPlayers()) {
			p.sendMessage(message);
		}
		Bukkit.getLogger().info(message);
	}

	private File getMineFile(Mine mine) {
		return new File(new File(getDataFolder(), "mines"), mine.getName().replace(" ", "") + ".mine.yml");
	}
	
	public void eraseMine(Mine mine) {
		mines.remove(mine);
		getMineFile(mine).delete();
	}

	
	public WorldEditPlugin getWorldEdit() {
		return worldEdit;
	}
	


}
