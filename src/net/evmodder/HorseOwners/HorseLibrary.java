package net.evmodder.HorseOwners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

public class HorseLibrary {
//	public static SpeedCalculator speedCalc = new SpeedCalculator(/*org.bukkit.Bukkit.getBukkitVersion()*/);
	//--------------- Library functions -----------------------------------------------------
	public static boolean isHorseFood(Material mat){
		switch(mat){
			case WHEAT:
			case HAY_BLOCK:
			case SUGAR:
			case APPLE:
			case GOLDEN_APPLE:
			case ENCHANTED_GOLDEN_APPLE:
			case GOLDEN_CARROT:
				return true;
			default:
				return false;
		}
	}

	public static boolean isBreedingFood(Material mat){
		switch(mat){
			case GOLDEN_APPLE:
			case ENCHANTED_GOLDEN_APPLE:
			case GOLDEN_CARROT:
				return true;
			default:
				return false;
		}
	}

	public static String cleanName(String horseName){
		return horseName == null ? null :
			ChatColor.stripColor(horseName).toLowerCase().replaceAll("[^a-z0-9_]", "");
	}

	public static boolean notFar(Location from, Location to){
		int x1 = from.getBlockX(), y1 = from.getBlockY(), z1 = from.getBlockZ();
		int x2 = to.getBlockX(), y2 = to.getBlockY(), z2 = to.getBlockZ();

		return(Math.abs(Math.abs(x1)-Math.abs(x2)) < 20
			&& Math.abs(Math.abs(y1)-Math.abs(y2)) < 6
			&& Math.abs(Math.abs(z1)-Math.abs(z2)) < 20
			&& from.getWorld().getName().equals(to.getWorld().getName()));
	}

	public static boolean safeForHorses(Location loc){
		int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
		World w = loc.getWorld();
		for(int cx = -1; cx <= 1; ++cx){
			for(int cz = -1; cz <= 1; ++cz){
				for(int cy = 0; cy <= 2; ++cy){
					if(w.getBlockAt(x +cx, y +cy, z+cz).getType().isOccluding()) return false;
				}
			}
		}
		return true;
	}

	public static void teleportEntityWithPassengers(Entity e, Location toLoc){
		if(e.getPassengers() == null || e.getPassengers().isEmpty()){
			if(!e.getLocation().getChunk().isLoaded()) e.getLocation().getChunk().load();
			if(!toLoc.getChunk().isLoaded()) toLoc.getChunk().load();
			e.teleport(toLoc);
		}
		else{
			List<Entity> passengers = e.getPassengers();
			e.eject();
			for(Entity passenger : passengers){
				teleportEntityWithPassengers(passenger, toLoc);
			}
			e.teleport(toLoc);
			for(Entity passenger : passengers){
				e.addPassenger(passenger);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void teleportEntityWithPassengersOld(Entity e, Location toLoc){
		if(e.getPassenger() == null){
			HorseManager.getPlugin().getLogger().info("No passenger");
			if(!e.getLocation().getChunk().isLoaded()) e.getLocation().getChunk().load();
			e.teleport(toLoc);
		}
		else{
			List<Entity> passengers = new ArrayList<Entity>();
			Entity nextPassenger = e;
			do{
				passengers.add(nextPassenger.getPassenger());
				nextPassenger.eject();

				if(!nextPassenger.getLocation().getChunk().isLoaded()) nextPassenger.getLocation().getChunk().load();
				nextPassenger.teleport(toLoc);
				nextPassenger = passengers.get(passengers.size()-1);
			}while(nextPassenger != null);

			nextPassenger = e;
			for(Entity passenger : passengers){
				nextPassenger.setPassenger(passenger);
				nextPassenger = passenger;//last value in "passengers" will be null.
			}
		}
	}

	public static AbstractHorse findAnyHorse(String horseName, World... worlds){
		horseName = cleanName(horseName);
		for(World world : (worlds == null ? org.bukkit.Bukkit.getWorlds() : Arrays.asList(worlds))){
			for(AbstractHorse horse : world.getEntitiesByClass(AbstractHorse.class)){
				if(horse.getCustomName() != null && cleanName(horse.getCustomName()).equals(horseName)){
					return horse;
				}
			}
		}
		return null;
	}

	public static boolean isLeashableBlock(Material mat){
		switch(mat){
			case OAK_FENCE:
			case ACACIA_FENCE:
			case NETHER_BRICK_FENCE:
			case BIRCH_FENCE:
			case SPRUCE_FENCE:
			case JUNGLE_FENCE:
			case DARK_OAK_FENCE:
				return true;
			default:
				return false;
		}
	}

	public static boolean containsIgnoreCaseAndColor(Set<String> list, String search){
		search = ChatColor.stripColor(search);
		for(String test : list) if(ChatColor.stripColor(test).equalsIgnoreCase(search)) return true;
		return false;
	}

/*	public static boolean isSuffocating(Block b){
	Material mat = b.getType();
	if(mat != Material.AIR && mat != Material.TORCH && mat != Material.CARPET && mat != Material.BED &&
	mat != Material.FENCE && mat != Material.FENCE_GATE && mat != Material.DEAD_BUSH && mat != Material.FLOWER_POT &&
	mat != Material.RED_ROSE && mat != Material.YELLOW_FLOWER && mat != Material.SKULL_ITEM && mat != Material.CAKE_BLOCK &&
	mat != Material.ACTIVATOR_RAIL && mat != Material.RAILS && mat != Material.POWERED_RAIL && mat != Material.VINE &&
	mat != Material.SIGN_POST && mat != Material.WALL_SIGN && mat != Material.SUGAR_CANE_BLOCK && mat != Material.WOOD_DOOR &&
	mat != Material.WEB && mat != Material.TRAP_DOOR &&  mat != Material.WOOD_STAIRS && mat != Material.TRAPPED_CHEST &&
	mat != Material.WOOD_BUTTON && mat != Material.CHEST && mat != Material.LEVER && mat != Material.LEAVES &&
	mat != Material.SNOW && mat != Material.DIODE_BLOCK_OFF && mat != Material.REDSTONE && mat != Material.REDSTONE_TORCH_ON &&
	mat != Material.REDSTONE_TORCH_OFF && mat != Material.DIODE_BLOCK_ON && mat != Material.GLASS && mat != Material.IRON_FENCE &&
	mat != Material.BREWING_STAND && mat != Material.STAINED_GLASS_PANE && mat != Material.DETECTOR_RAIL && mat != Material.DEAD_BUSH
		) return true;
		else return false;
	}*/

	public static void setMother(Entity horse, String mother){
		horse.setMetadata("mother", new FixedMetadataValue(HorseManager.getPlugin(), cleanName(mother)));
	}
	public static void setFather(Entity horse, String father){
		horse.setMetadata("father", new FixedMetadataValue(HorseManager.getPlugin(), cleanName(father)));
	}

	public static String getMother(Entity horse){
		return horse.hasMetadata("mother") ? horse.getMetadata("mother").get(0).asString() : null;
	}
	public static String getFather(Entity horse){
		return horse.hasMetadata("father") ? horse.getMetadata("father").get(0).asString() : null;
	}

	public static void setTimeBorn(Entity horse, long timestamp){
		horse.setMetadata("spawn_ts", new FixedMetadataValue(HorseManager.getPlugin(), timestamp));
	}
	public static Long getTimeBorn(Entity horse){
		return horse.hasMetadata("spawn_ts") ? horse.getMetadata("spawn_ts").get(0).asLong() : null;
	}
	public static void setTimeClaimed(Entity horse, long timestamp){
		horse.setMetadata("claim_ts", new FixedMetadataValue(HorseManager.getPlugin(), timestamp));
	}
	public static Long getTimeClaimed(Entity horse){
		return horse.hasMetadata("claim_ts") ? horse.getMetadata("claim_ts").get(0).asLong() : null;
	}

	public static double getNormalSpeed(Attributable horse){
		return normalizeSpeed(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());
	}
	public static double getNormalJump(AbstractHorse horse){
		return normalizeJump(horse.getJumpStrength());
	}
	public static int getNormalMaxHealth(Attributable horse){
		return (int)Math.rint(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
	}
	public static int getNormalCurrentHealth(LivingEntity horse){
		return (int)horse.getHealth();
	}
	public static double normalizeSpeed(double speed){
		return Math.rint(speed/0.0002325581395348837209302325581)/100;
	}
	public static double normalizeJump(double jump){
		if(0.432084373616155 <= jump && jump <= 0.966967527085333){
			jump = -0.343930367 + 2.128599134*jump + 3.689713992*jump*jump - 0.1817584952*jump*jump*jump;
			return Math.rint(jump*100)/100;
		}
		else return Math.rint(jump*500)/100;
	}
	public static double denormalizeSpeed(double speed){
		return 0.02325581395348837209302325581*speed;
	}
	public static double denormalizeJump(double jump){
		if(1.25 <= jump && jump <= 5)
			return 0.1675804 + 0.237535*jump - 0.0223934*jump*jump + 0.00137289*jump*jump*jump;
		else
			return 0.2*jump;
	}
	/*public static <E extends Attributable & Damageable> void setMaxHealth(E target, double health){
		target.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		target.setHealth(health);
	}*/
}