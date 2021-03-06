package net.evmodder.HorseOwners.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.evmodder.EvLib.hooks.MultiverseHook;
import net.evmodder.HorseOwners.HorseUtils;

public class CommandGetHorse extends HorseCommand{
	boolean saveCoords, safeTeleports, allowTransworld;

	public CommandGetHorse(){
		saveCoords = plugin.getConfig().getBoolean("save-horse-coordinates");
		safeTeleports = plugin.getConfig().getBoolean("teleport-only-if-safe");
		allowTransworld = plugin.getConfig().getBoolean("teleport-across-worlds");
	}

	@Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length > 0 && sender instanceof Player){
			final String arg = HorseUtils.cleanName(String.join(" ", args));
			//TODO: possible (but maybe laggy?): only list horses in same world if player lacks cross-world permission
			return plugin.getAPI().getHorses(((Player)sender).getUniqueId())
					.stream().filter(name -> name.startsWith(arg)).limit(20).collect(Collectors.toList());
		}
		return null;
	}

	@Override
	public boolean onHorseCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/hm get [horse]
		if(sender instanceof Player == false){
			sender.sendMessage(ChatColor.RED+"This command can only be run by in-game players");
			COMMAND_SUCCESS = false;
			return true;
		}
		if(args.length < 1){
			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
			COMMAND_SUCCESS = false;
			return false;
		}
		Player p = (Player) sender;
		String target = String.join(" ", args);
		Entity horse;
		Set<Entity> horses = new HashSet<Entity>();

		if(safeTeleports && HorseUtils.safeForHorses(p.getLocation()) == false){
			p.sendMessage(ChatColor.RED
					+"Unable to teleport horse - Please move to a more open area to prevent risk of horse suffocation");
			COMMAND_SUCCESS = false;
			return true;
		}

		if(target.toLowerCase().equals("all")  || target.toLowerCase().equals("@a")){
			int lost = 0;
			World[] worlds = new World[]{p.getWorld()};//restrict to just this world

			if(allowTransworld){
				if(p.hasPermission("horseowners.tpansworld.*"))/* worlds = null*/;//already null
				else if(p.hasPermission("horseowners.tpansworld.samegamemode")){
					List<World> worldsList = new ArrayList<World>();
					for(World w : plugin.getServer().getWorlds()){
						GameMode gm = MultiverseHook.getWorldGameMode(w);
						if(gm == null || p.getGameMode() == gm) worldsList.add(w);
					}
					worlds = worldsList.toArray(worlds);
				}
			}

			for(String cleanHorseName : plugin.getAPI().getHorses(p.getUniqueId())){
				p.sendMessage("Fetching: "+cleanHorseName);
				horse = plugin.getAPI().getHorse(cleanHorseName, /*loadChunk=*/true);
				if(horse != null) horses.add(horse);
				else ++lost;
			}
			//Sometimes some of the horses won't make it
			if(lost > 0) p.sendMessage(ChatColor.YELLOW+"Unable to locate "
						+ChatColor.GRAY+lost+ChatColor.YELLOW+" of your horses");
			if(horses.size() == 0){
				COMMAND_SUCCESS = false;
				return true;
			}
		}
		else{
			String cleanHorseName = HorseUtils.cleanName(target);
			if(plugin.getAPI().isClaimedHorse(cleanHorseName) == false){
				sender.sendMessage(ChatColor.RED+"Unknown horse '"+ChatColor.GRAY+target+ChatColor.RED+'\'');
//				sender.sendMessage("�cUnclaimed horses cannot be teleported via command, you must first use /claimhorse");
				COMMAND_SUCCESS = false;
				return false;
			}
			else if(plugin.getAPI().canAccess(p.getUniqueId(), target) == false){
				p.sendMessage(ChatColor.RED+"You may not teleport horses which you do not own");
				COMMAND_SUCCESS = false;
				return true;
			}
			horse = plugin.getAPI().getHorse(cleanHorseName, /*loadChunk=*/true);
//			horse = HorseLibrary.findAnyHorse(target);
			if(horse == null){
				p.sendMessage(ChatColor.RED+"Unable to find your horse! Perhaps the chunk it was in is unloaded?");
				COMMAND_SUCCESS = false;
				return true;
			}
			if(horse.getWorld().getUID().equals(p.getWorld().getUID()) == false){
				GameMode gm = MultiverseHook.getWorldGameMode(horse.getWorld());
				if(!allowTransworld || (!p.hasPermission("horseowners.crossworld.anywhere") &&
						(!p.hasPermission("horseowners.crossworld.samegamemode") ||
								(gm != null && p.getGameMode() != gm))))
				{
					p.sendMessage(ChatColor.RED+"Unable to teleport the horse, "
								+ChatColor.GRAY+horse.getCustomName()+ChatColor.RED+"--");
					p.sendMessage(ChatColor.RED+"You do not have permission to use trans-world horse teleportation");
					COMMAND_SUCCESS = false;
					return true;
				}
				else p.sendMessage(ChatColor.GRAY+"Attempting to fetch horse from world: "
								+ChatColor.GREEN+horse.getWorld().getName());
			}
			horses.add(horse);
		}

		//Yay got to here! Hi horsie!
		for(Entity h : horses){
			HorseUtils.teleportEntityWithPassengers(h, p.getLocation());
			if(saveCoords) plugin.getAPI().updateDatabase(h);
		}
		p.sendMessage(ChatColor.GREEN+"Fetched your horse"+(horses.size() > 1 ? "s!" : "!"));
		COMMAND_SUCCESS = true;
		return true;
	}
}