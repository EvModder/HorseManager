package Evil_Code_HorseOwners.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class CommandAllowRide extends HorseCommand{
	
	@Override
	public boolean onHorseCommand(CommandSender sender, Command command, String label, String args[]){
		//cmd:	/hm allowride <Player> [horse]

		if(args.length == 0){
			sender.sendMessage(ChatColor.RED+"Too few arguments!"+ChatColor.GRAY+'\n'+command.getUsage());
			return false;
		}

		@SuppressWarnings("deprecation")
		Player recipient = sender.getServer().getPlayer(args[0]);
		if(recipient == null){
			sender.sendMessage(ChatColor.RED+"Player \""+ChatColor.GOLD+args[0]
					+ChatColor.RED+"\" not found!"+ChatColor.GRAY+'\n'+command.getUsage());
			return false;
		}

		Player p = (sender instanceof Player) ? (Player)sender : null;
		String horseName;
		if(args.length > 1) horseName = StringUtils.join(args, ' ', 1, args.length);
		else if(p != null && p.isInsideVehicle() && p.getVehicle() instanceof Horse){
			if((horseName=p.getVehicle().getCustomName()) == null || plugin.isClaimedHorse(horseName) == false){
				sender.sendMessage(ChatColor.GRAY+"This horse is ownerless, anyone can ride it!");
				return false;
			}
		}
		else{
			sender.sendMessage(ChatColor.RED+"Please specify both a horse and a player"
						+ChatColor.GRAY+'\n'+command.getUsage());
			return false;
		}

		if(plugin.isClaimedHorse(horseName) == false){
			sender.sendMessage(ChatColor.RED+"This horse is ownerless, anyone can ride it!");
			return false;
		}
		else if(p != null && plugin.canAccess(p, horseName) == false){
			sender.sendMessage(ChatColor.RED+"You do not own this horse");
			return false;
		}

		//Yay got to here! Now give them a boost up!
		plugin.grantOneTimeAccess(recipient.getUniqueId(), horseName);

		//Tell the sender
		sender.sendMessage(ChatColor.GREEN+"Player "+ChatColor.GRAY+args[0]
					+ChatColor.GREEN+" is now allowed one ride on the horse, "
					+ChatColor.GRAY+horseName+ChatColor.GREEN+'.');
		
		//Tell the allowed rider
		recipient.sendMessage(ChatColor.GRAY+sender.getName()+ChatColor.GREEN
				+" has granted you permission for a single ride on their horse, "
				+ChatColor.GRAY+horseName+ChatColor.GREEN+'.');
		return true;
	}
}