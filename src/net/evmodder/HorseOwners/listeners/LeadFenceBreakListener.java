package net.evmodder.HorseOwners.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import net.evmodder.HorseOwners.HorseUtils;
import net.evmodder.HorseOwners.HorseManager;

public class LeadFenceBreakListener implements Listener{
	private HorseManager plugin;

	public LeadFenceBreakListener(){
		plugin = HorseManager.getPlugin();
	}

	//Disable lead-hitch fence breaking
	@EventHandler
	public void onBlockBreak(BlockBreakEvent evt){
		if(HorseUtils.isLeashableBlock(evt.getBlock().getType())){
			for(Entity leash : evt.getBlock().getChunk().getEntities()){
				if(leash.getType() == EntityType.LEASH_HITCH
						&& leash.getLocation().equals(evt.getBlock().getLocation())){

					for(Entity e : leash.getNearbyEntities(15, 15, 15)){
						if(e instanceof AbstractHorse && e.getCustomName() != null){
							AbstractHorse h = (AbstractHorse) e;
							if(h.isLeashed() && h.getLeashHolder().getUniqueId().equals(leash.getUniqueId())){
								
								if(!plugin.getAPI().canAccess(evt.getPlayer().getUniqueId(), HorseUtils.cleanName(h.getCustomName()))){
									evt.setCancelled(true);
									evt.getPlayer().sendMessage(ChatColor.RED+
											"You do not have permission to unleash this horse.");
								}
							}//if the leash holder of this horse is a leash on the broken block
						}//if the entity is a horse
					}//loop through entities around the leash hitch
				}//if this leash hitch is attached to the block that was broken
			}//loop through entities in this chunk looking for leashes
		}//if the broken block is of a type that can hold leashes
	}//block break event
}