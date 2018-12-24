package com.koletar.jj.mineresetlite.command.commands;

import com.koletar.jj.mineresetlite.*;
import com.koletar.jj.mineresetlite.command.Command;
import com.koletar.jj.mineresetlite.debugger.MineDebugger;
import com.koletar.jj.mineresetlite.util.StringTools;
import com.koletar.jj.mineresetlite.util.XMaterial;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

import static com.koletar.jj.mineresetlite.util.Phrases.phrase;

/**
 * @author jjkoletar
 */
public class MineCommands {
	private MineResetLite plugin;
	private Map<Player, Location> point1;
	private Map<Player, Location> point2;
	
	public MineCommands(MineResetLite plugin) {
		this.plugin = plugin;
		point1 = new HashMap<>();
		point2 = new HashMap<>();
	}
	
	@Command(aliases = {"list", "l"},
			description = "List the names of all Mines",
			permissions = {"mineresetlite.mine.list"},
			help = {"List the names of all Mines currently created, across all worlds."},
			min = 0, max = 0, onlyPlayers = false)
	public void listMines(CommandSender sender, String[] args) {
		sender.sendMessage(phrase("mineList", StringTools.buildList(plugin.mines, "&c", "&d, ")));
	}
	
	@Command(aliases = {"pos1", "p1"},
			description = "Change your first selection point",
			help = {"Run this command to set your first selection point to the block you are looking at.",
					"Use /mrl pos1 -feet to set your first point to the location you are standing on."},
			usage = "(-feet)",
			permissions = {"mineresetlite.mine.create", "mineresetlite.mine.redefine"},
			min = 0, max = 1, onlyPlayers = true)
	public void setPoint1(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Player player = (Player) sender;
		point1 = setPoint(player,args);
		if(!point1.isEmpty()){
			player.sendMessage(phrase("firstPointSet"));
		} else {
			//Args weren't empty or -feet, bad args
			throw new InvalidCommandArgumentsException();
		}
	}
	
	@Command(aliases = {"pos2", "p2"},
			description = "Change your second selection point",
			help = {"Run this command to set your second selection point to the block you are looking at.",
					"Use /mrl pos2 -feet to set your second point to the location you are standing on."},
			usage = "(-feet)",
			permissions = {"mineresetlite.mine.create", "mineresetlite.mine.redefine"},
			min = 0, max = 1, onlyPlayers = true)
	public void setPoint2(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Player player = (Player) sender;
		point2 = setPoint(player, args);
		if (!point2.isEmpty()) {
			player.sendMessage(phrase("secondPointSet"));
		}
		else {
			throw new InvalidCommandArgumentsException();
		}
	}

	/**
	 * Sets the point according to the block looked at
	 * or the block stood on.
	 * @param player the player that runs the command
	 * @param args commands arguments
	 * @return the point set by the player
	 */
	 private Map<Player,Location> setPoint(Player player, String[] args) {
		 HashMap<Player,Location> point = new HashMap<>();
		 if (args.length == 0) {
			 //Use block being looked at
			 point.put(player, player.getTargetBlock(null, 100).getLocation());
			 return point;
		 } else if (args[0].equalsIgnoreCase("-feet")) {
			 //Use block being stood on
			 point.put(player, player.getLocation());
			 return point;
		 }
		 return point;
	 }
	/**
	 * Sorts & Swaps the 2 points,
	 * p1 is smaller, p2 is bigger.
	 * @param p1 a point
	 * @param p2 a point
	 */
	private void sortCoordinates(Vector p1, Vector p2){
		if (p1.getX() > p2.getX()) {
			//Swap
			double x = p1.getX();
			p1.setX(p2.getX());
			p2.setX(x);
		}
		if (p1.getY() > p2.getY()) {
			double y = p1.getY();
			p1.setY(p2.getY());
			p2.setY(y);
		}
		if (p1.getZ() > p2.getZ()) {
			double z = p1.getZ();
			p1.setZ(p2.getZ());
			p2.setZ(z);
		}
	}

	/**
	 * Checks if mine name is unique
	 * @param name mine name
	 * @return if the name is unique
	 */
	private boolean isUniqueName(String name){
		Mine[] mines = plugin.matchMines(name);
		return mines.length == 0;
	}

	@Command(aliases = {"create", "save"},
			description = "Create a mine from either your WorldEdit selection or by manually specifying the points",
			help = {"Provided you have a selection made via either WorldEdit or selecting the points using MRL,",
					"an empty mine will be created. This mine will have no composition and default settings."},
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.create"},
			min = 1, max = -1, onlyPlayers = true)
	public void createMine(CommandSender sender, String[] args) {
		//Find out how they selected the region
		Player player = (Player) sender;
		World world = null;
		Vector p1 = null;
		Vector p2 = null;

		//Native selection techniques?
		if (point1.containsKey(player) && point2.containsKey(player)) {
			world = point1.get(player).getWorld();
			if (!world.equals(point2.get(player).getWorld())) {
				player.sendMessage(phrase("crossWorldSelection"));
				return;
			}
			p1 = point1.get(player).toVector();
			p2 = point2.get(player).toVector();
		}

		// Converts the selection to a vector, weird way to do this,
		// there should be a way to just make it a rounded location.
		// selection
		if(plugin.getWorldEdit()!=null) {
			Selection selection = plugin.getWorldEdit().getSelection(player);
			if (selection != null) {
				world = selection.getWorld();
				p1 = selection.getMinimumPoint().toVector();
				p2 = selection.getMaximumPoint().toVector();
			}
		}
		
		if (p1 == null) {
			player.sendMessage(phrase("emptySelection"));
			return;
		}

		//Construct mine name
		String name = StringTools.buildSpacedArgument(args);
		if (!isUniqueName(name)) {
			player.sendMessage(phrase("nameInUse", name));
			return;
		}
		sortCoordinates(p1,p2);

		//Create!
        Position minPos = new Position(p1.getBlockX(),p1.getBlockY(),p1.getBlockZ());
        Position maxPos = new Position(p2.getBlockX(),p2.getBlockY(),p2.getBlockZ());

        Mine newMine = new Mine(minPos,maxPos,name,world);
		plugin.mines.add(newMine);
		player.sendMessage(phrase("mineCreated", newMine));

		if(Config.isDebug())
		    Bukkit.broadcastMessage(MineDebugger.toString(newMine));

		plugin.buffSave();
	}
	
	@Command(aliases = {"info", "i"},
			description = "List information about a mine",
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.info"},
			min = 1, max = -1, onlyPlayers = false)
	public void mineInfo(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args));
		if (invalidMines(sender, mines)) return;
		sender.sendMessage(phrase("mineInfoName", mines[0]));
		sender.sendMessage(phrase("mineInfoWorld", mines[0].getWorld()));

		//Build composition list
		StringBuilder csb = new StringBuilder();
		for (Map.Entry<Material, Double> entry : mines[0].getComposition().entrySet()) {
			csb.append(entry.getValue() * 100);
			csb.append("% ");
			csb.append(Material.getMaterial("" + entry.getKey()).toString());
			/*
			if (entry.getKey().getData() != 0) {
				csb.append(":");
				csb.append(entry.getKey().getData());
			}*/
			csb.append(", ");
		}
		if (csb.length() > 2) {
			csb.delete(csb.length() - 2, csb.length() - 1);
		}
		sender.sendMessage(phrase("mineInfoComposition", csb));
		if (mines[0].getResetDelay() != 0) {
			sender.sendMessage(phrase("mineInfoResetDelay", mines[0].getResetDelay()));
			sender.sendMessage(phrase("mineInfoTimeUntilReset", mines[0].getTimeUntilReset()));
		}
		sender.sendMessage(phrase("mineInfoSilence", mines[0].isSilent()));
		if (mines[0].getResetWarnings().size() > 0) {
			sender.sendMessage(phrase("mineInfoWarningTimes", StringTools.buildList(mines[0].getResetWarnings(), "", ", ")));
		}
		if (mines[0].getSurface() != null) {
			sender.sendMessage(phrase("mineInfoSurface", mines[0].getSurface()));
		}
		if (mines[0].getFillMode()) {
			sender.sendMessage(phrase("mineInfoFillMode"));
		}
	}
	
	private boolean invalidMines(CommandSender sender, Mine[] mines) {
		if (mines.length > 1) {
			sender.sendMessage(phrase("tooManyMines", plugin.toString(mines)));
			return true;
		} else if (mines.length == 0) {
			sender.sendMessage(phrase("noMinesMatched"));
			return true;
		}
		return false;
	}

	private boolean isMaterial(Material material, CommandSender sender){
		boolean isMaterial = true;
		if(material == null){
			sender.sendMessage(phrase("unknownBlock"));
			isMaterial = false;
		}
		if(!material.isBlock()){
			sender.sendMessage(phrase("notABlock"));
			isMaterial = false;
		}
		return isMaterial;
	}

    /** TODO: Reduce complexity of method
     *  Sets the composition of a mine
	 * @param args
	 * @param sender Is the sender, can be console or player
     */
	@Command(aliases = {"set", "add", "+"},
			description = "Set the percentage of a block in the mine",
			help = {"This command will always overwrite the current percentage for the specified block,",
					"if a percentage has already been set. You cannot set the percentage of any specific",
					"block, such that the percentage would then total over 100%."},
			usage = "<mine name> <block>:(data) <percentage>%",
			permissions = {"mineresetlite.mine.composition"},
			min = 3, max = -1, onlyPlayers = false)
	public void setComposition(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args, 2));
		if (invalidMines(sender, mines)) {
			return;
		}

		//Match material
		//String[] bits = args[args.length - 2].split(":");
		String strBlock = args[args.length-2];
		//Material material = plugin.matchMaterial(bits[0]);
		Material material = XMaterial.fromString(strBlock).parseMaterial();

		if(!isMaterial(material,sender)){
			return;
		}
		/*
		if (material == null) {
			sender.sendMessage(phrase("unknownBlock"));
			return;
		}
		if (!material.isBlock()) {
			sender.sendMessage(phrase("notABlock"));
			return;
		}*/

		/*
		byte data = 0;
		if (bits.length == 2) {
			try {
				data = Byte.valueOf(bits[1]);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(phrase("unknownBlock"));
				return;
			}
		}*/

		// Parse percentage
		// Prone to mistakes from the users side, shouldn't have to add '%' to the command.
		String percentageS = args[args.length - 1];
		/*if (!percentageS.endsWith("%")) {
			sender.sendMessage(phrase("badPercentage"));
			return;
		}*/
		StringBuilder psb = new StringBuilder(percentageS);
		if(percentageS.endsWith("%")) {
			psb.deleteCharAt(psb.length() - 1); //deletes the '%' should be stripped out only if it exists
		}
		double percentage;
		try {
			percentage = Double.valueOf(psb.toString());
		} catch (NumberFormatException nfe) {
			sender.sendMessage(phrase("badPercentage"));
			return;
		}
		if (percentage > 100 || percentage <= 0) {
			sender.sendMessage(phrase("badPercentage"));
			return;
		}
		percentage = percentage / 100; //Make it a programmatic percentage
		//SerializableBlock block = new SerializableBlock(material.getId(), data);
		Double oldPercentage = mines[0].getComposition().get(material);
		double total = 0;
		for (Map.Entry<Material, Double> entry : mines[0].getComposition().entrySet()) {
			if (!entry.getKey().equals(material)) {
				total += entry.getValue();
			}
		}
		total += percentage;
		if (total > 1) {
			sender.sendMessage(phrase("insaneCompositionChange"));
			if (oldPercentage == null) {
				mines[0].getComposition().remove(material);
			} else {
				mines[0].getComposition().put(material, oldPercentage);
			}
			return;
		}
		mines[0].getComposition().put(material, percentage);
		sender.sendMessage(phrase("mineCompositionSet", mines[0], percentage * 100, material, (1 - mines[0].getCompositionTotal()) * 100));
		plugin.buffSave();
	}
	
	@Command(aliases = {"unset", "remove", "-"},
			description = "Remove a block from the composition of a mine",
			usage = "<mine name> <block>:(data)",
			permissions = {"mineresetlite.mine.composition"},
			min = 2, max = -1, onlyPlayers = false)
	public void unsetComposition(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args, 1));
		if (invalidMines(sender, mines)) {
			return;
		}
		//Match material
		//String[] bits = args[args.length - 1].split(":");
		//Material material = plugin.matchMaterial(bits[0]);
		Material material = XMaterial.fromString(args[args.length-1]).parseMaterial();
		if(isMaterial(material,sender))
			return;
		/*
		if (material == null) {
			sender.sendMessage(phrase("unknownBlock"));
			return;
		}
		if (!material.isBlock()) {
			sender.sendMessage(phrase("notABlock"));
			return;
		}*/
		/*
		byte data = 0;
		if (bits.length == 2) {
			try {
				data = Byte.valueOf(bits[1]);
			} catch (NumberFormatException nfe) {
				sender.sendMessage(phrase("unknownBlock"));
				return;
			}
		}*/
		//Does the mine contain this block?
		//SerializableBlock block = new SerializableBlock(material.getId(), data);
		for (Map.Entry<Material, Double> entry : mines[0].getComposition().entrySet()) {
			if (entry.getKey().equals(material)) {
				mines[0].getComposition().remove(entry.getKey());
				sender.sendMessage(phrase("blockRemovedFromMine", mines[0], material, (1 - mines[0].getCompositionTotal()) * 100));
				return;
			}
		}
		sender.sendMessage(phrase("blockNotInMine", mines[0], material));
		plugin.buffSave();
	}
	
	@Command(aliases = {"reset", "r"},
			description = "Reset a mine",
			help = {"If you supply the -s argument, the mine will silently reset. Resets triggered via",
					"this command will not show a 1 minute warning, unless this mine is flagged to always",
					"have a warning. If the mine's composition doesn't equal 100%, the composition will be",
					"padded with air until the total equals 100%."},
			usage = "<mine name> (-s)",
			permissions = {"mineresetlite.mine.reset"},
			min = 1, max = -1, onlyPlayers = false)
	public void resetMine(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args).replace(" -s", ""));
		if (invalidMines(sender, mines)) return;
		if (args[args.length - 1].equalsIgnoreCase("-s")) {
			//Silent reset
			mines[0].reset();
		} else {
			MineResetLite.broadcast(phrase("mineResetBroadcast", mines[0], sender), mines[0]);
			mines[0].reset();
		}
	}

    /** TODO: Reduce complexity
     */
	@Command(aliases = {"flag", "f"},
			description = "Set various properties of a mine, including automatic resets",
			help = {"Available flags:",
					"resetPercent: A integer number (0 < x < 100) specifying the percentage of mined blocks triggering the reset. Set to -1 to disable automatic percent resets.",
					"resetDelay: An integer number of minutes specifying the time between automatic resets. Set to 0 to disable automatic resets.",
					"resetWarnings: A comma separated list of integer minutes to warn before the automatic reset. Warnings must be less than the reset delay.",
					"surface: A block that will cover the entire top surface of the mine when reset, obscuring surface ores. Set surface to air to clear the value.",
					"fillMode: An alternate reset algorithm that will only \"reset\" air blocks inside your mine. Set to true or false.",
					"isSilent: A boolean (true or false) of whether or not this mine should broadcast a reset notification when it is reset *automatically*"},
			usage = "<mine name> <setting> <value>",
			permissions = {"mineresetlite.mine.flag"},
			min = 3, max = -1, onlyPlayers = false)
	public void flag(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args, 2));
		if (invalidMines(sender, mines)) return;
		String setting = args[args.length - 2];
		String value = args[args.length - 1];

		switch (setting.toUpperCase()){
			case "RESETEVERY":
			case "RESETDELAY": {
				int delay;
				try {
					delay = Integer.valueOf(value);
				} catch (NumberFormatException nfe) {
					sender.sendMessage(phrase("badResetDelay"));
					return;
				}
				if (delay < 0) {
					sender.sendMessage(phrase("badResetDelay"));
					return;
				}
				mines[0].setResetDelay(delay);
				if (delay == 0) {
					sender.sendMessage(phrase("resetDelayCleared", mines[0]));
				} else {
					sender.sendMessage(phrase("resetDelaySet", mines[0], delay));
				}
				plugin.buffSave();
				break;
			}
			case "RESETWARNINGS":
			case "RESETWARNING": {
				String[] bits = value.split(",");
				List<Integer> warnings = mines[0].getResetWarnings();
				List<Integer> oldList = new LinkedList<>(warnings);
				warnings.clear();
				for (String bit : bits) {
					try {
						warnings.add(Integer.valueOf(bit));
					} catch (NumberFormatException nfe) {
						sender.sendMessage(phrase("badWarningList"));
						return;
					}
				}
				//Validate warnings
				for (Integer warning : warnings) {
					if (warning >= mines[0].getResetDelay()) {
						sender.sendMessage(phrase("badWarningList"));
						mines[0].setResetWarnings(oldList);
						return;
					}
				}
				if (warnings.contains(0) && warnings.size() == 1) {
					warnings.clear();
					sender.sendMessage(phrase("warningListCleared", mines[0]));
					return;
				} else if (warnings.contains(0)) {
					sender.sendMessage(phrase("badWarningList"));
					mines[0].setResetWarnings(oldList);
					return;
				}
				sender.sendMessage(phrase("warningListSet", mines[0]));
				plugin.buffSave();
				break;
			}
			case "SURFACE": {
				Material m = XMaterial.fromString(value).parseMaterial();
				if (m == null) {
					sender.sendMessage(phrase("unknownBlock"));
					return;
				}
				if (!m.isBlock()) {
					sender.sendMessage(phrase("notABlock"));
					return;
				}

				if (m.equals(Material.AIR)) {
					mines[0].setSurface(null);
					sender.sendMessage(phrase("surfaceBlockCleared", mines[0]));
					plugin.buffSave();
					return;
				}
				//SerializableBlock block = new SerializableBlock(m.getId(), data);
				mines[0].setSurface(m);
				sender.sendMessage(phrase("surfaceBlockSet", mines[0]));
				plugin.buffSave();
				break;
			}
			case "FILL":
			case "FILLMODE": {
				//Match true or false
				if (trueValue(value)) {
					mines[0].setFillMode(true);
					sender.sendMessage(phrase("fillModeEnabled"));
					plugin.buffSave();
					return;
				} else if (falseValue(value)) {
					mines[0].setFillMode(false);
					sender.sendMessage(phrase("fillModeDisabled"));
					plugin.buffSave();
					return;
				}
				sender.sendMessage(phrase("invalidFillMode"));
				break;
			}
			case "ISSILENT":
			case "SILENT":
			case "SILENCE": {
				if (trueValue(value)) {
					mines[0].setSilence(true);
					sender.sendMessage(phrase("mineIsNowSilent", mines[0]));
					plugin.buffSave();
					return;
				} else if (falseValue(value)) {
					mines[0].setSilence(false);
					sender.sendMessage(phrase("mineIsNoLongerSilent", mines[0]));
					plugin.buffSave();
					return;
				}
				sender.sendMessage(phrase("badBoolean"));
				break;
			}
			case "RESETPERCENT":{
				StringBuilder psb = new StringBuilder(value);
				psb.deleteCharAt(psb.length() - 1);
				double percentage;
				try {
					percentage = Double.valueOf(psb.toString());
				} catch (NumberFormatException nfe) {
					sender.sendMessage(phrase("badPercentage"));
					return;
				}
				if (percentage > 100 || percentage <= 0) {
					sender.sendMessage(phrase("badPercentage"));
					return;
				}
				percentage = percentage / 100; //Make it a programmatic percentage
				mines[0].setResetPercent(percentage);

				if (percentage < 0) {
					sender.sendMessage(phrase("resetDelayCleared", mines[0]));
				} else {
					sender.sendMessage(phrase("resetPercentageSet", mines[0], (int) (percentage * 100)));
				}
				plugin.buffSave();
				return;
			}
			default: sender.sendMessage(phrase("unknownFlag"));
		}
	}

	private boolean trueValue(String value){
		value = value.toLowerCase();
		return (value.equals("true")||value.equals("yes")||value.equals("enabled"));
	}
	private boolean falseValue(String value){
		value = value.toLowerCase();
		return (value.equals("false")||value.equals("no")||value.equals("disabled"));
	}
	
	@Command(aliases = {"erase"},
			description = "Completely erase a mine",
			help = {"Like most erasures of data, be sure you don't need to recover anything from this mine before you delete it."},
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.erase"},
			min = 1, max = -1, onlyPlayers = false)
	public void erase(CommandSender sender, String[] args) {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args));
		if (invalidMines(sender, mines)) return;
		plugin.eraseMine(mines[0]);
		sender.sendMessage(phrase("mineErased", mines[0]));
	}
	
	@Command(aliases = {"reschedule"},
			description = "Synchronize all automatic mine resets",
			help = {"This command will set the 'start time' of the mine resets to the same point."},
			usage = "",
			permissions = {"mineresetlite.mine.flag"},
			min = 0, max = 0, onlyPlayers = false)
	public void reschedule(CommandSender sender, String[] args) {
		for (Mine mine : plugin.mines) {
			mine.setResetDelay(mine.getResetDelay());
		}
		plugin.buffSave();
		sender.sendMessage(phrase("rescheduled"));
	}
	
	@Command(aliases = {"tp", "teleport"},
			description = "Teleport to the specified mine",
			help = {"This command will teleport you to the center of the specified mine or at the teleport point if it is specified."},
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.tp"},
			min = 1, max = -1,
			onlyPlayers = true)
	public void teleport(CommandSender sender, String[] args) {
		Mine mine = null;
		
		for (Mine aMine : plugin.mines) {
			if (aMine.getName().equalsIgnoreCase(args[0])) {
				mine = aMine;
			}
		}
		
		if (mine == null) {
			sender.sendMessage(phrase("noMinesMatched"));
			return;
		}
		
		mine.teleport((Player) sender);
	}
	
	@Command(aliases = {"settp", "stp"},
			description = "Sets the specified mine's spawn point",
			help = {"This command will set the specified mine's reset spawn point to where you're standing.", "Use /mrl removetp <mine name> to remove the mine's spawn point."},
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.settp"},
			min = 1, max = -1,
			onlyPlayers = true)
	public void setTP(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Player player = (Player) sender;
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args));
		if (invalidMines(sender, mines)) return;
		mines[0].setTp(player.getLocation());
		plugin.buffSave();
		sender.sendMessage(phrase("tpSet", mines[0]));
	}
	
	@Command(aliases = {"removetp", "rtp"},
			description = "Removes the specified mine's spawn point",
			help = {"This comamnd will remove the specified mine's reset spawn point.", "Use /mrl removetp to remove the spawn point.", "use /mrl settp to set it to where you're standing."},
			usage = "<mine name>",
			permissions = {"mineresetlite.mine.removetp"},
			min = 1, max = -1,
			onlyPlayers = true)
	public void removeTP(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Player player = (Player) sender;
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args));
		if (invalidMines(sender, mines)) return;
		mines[0].setTp(new Location(player.getWorld(), 0, -Integer.MAX_VALUE, 0));
		plugin.buffSave();
		sender.sendMessage(phrase("tpRemove", mines[0]));
	}
	
	@Command(aliases = {"addpotion", "addpot"},
			description = "Adds the specified potion to the mine",
			help = {"This command will saddthe specified potion to the mine where you're standing.", "Use /mrl removepot <mine name> <potionname> to remove the specified potion effect from the mine."},
			usage = "<mine name> <potionname:amplifier>",
			permissions = {"mineresetlite.mine.addpotion"},
			min = 1, max = -1,
			onlyPlayers = true)
	public void addPot(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args, 1));
		if (invalidMines(sender, mines)) return;
		mines[0].addPotion(args[args.length - 1]);
		plugin.buffSave();
		sender.sendMessage(phrase("potionAdded", args[args.length - 1], mines[0]));
	}
	
	@Command(aliases = {"removepotion", "removepot"},
			description = "Removes the specified potion from the mine",
			help = {"This comamnd will remove the specified potion from the mine.", "Use /mrl removepot <potionname> to remove the potion.", "Use /mrl addpot <mine name> <potionname:amplifier> to add the specified potion effect to the mine."},
			usage = "<mine name> <potionname>",
			permissions = {"mineresetlite.mine.removepotion"},
			min = 1, max = -1,
			onlyPlayers = true)
	public void removePot(CommandSender sender, String[] args) throws InvalidCommandArgumentsException {
		Mine[] mines = plugin.matchMines(StringTools.buildSpacedArgument(args, 1));
		if (invalidMines(sender, mines)) return;
		mines[0].removePotion(args[args.length - 1]);
		plugin.buffSave();
		sender.sendMessage(phrase("potionRemoved", args[args.length - 1], mines[0]));
	}
}
