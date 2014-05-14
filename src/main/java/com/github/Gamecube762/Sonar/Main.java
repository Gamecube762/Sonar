package com.github.Gamecube762.Sonar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.darkblade12.particleeffect.ParticleEffect;

/**
 * Created by Gamecube762 on 5/11/14.
 */
public class Main extends JavaPlugin implements Listener {
	
	int refresh, warningParticleAmount;
	double searchDistance, viewDistance, warningDistance;
	
	//static to help with /reloading
	protected static ArrayList<String> SonarList = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);		
		
		refresh = getConfig().getInt("refreshRate");
		searchDistance = getConfig().getDouble("searchDistance");
		viewDistance = getConfig().getDouble("viewDistance");
		warningDistance = getConfig().getDouble("warningDistance");
		warningParticleAmount = getConfig().getInt("warningParticleAmount");
		
		getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				for (String s : SonarList) {
					Player p;
					try {
						p = Bukkit.getPlayer(s);
					} catch (NullPointerException e) {
						SonarList.remove(s);
						continue;
					}
					
					if (areMonstersNearby(p, warningDistance))
						showWarningParticles(p, warningParticleAmount, viewDistance);
					
                    if (!p.hasPermission("sonar.noDarkness")) {
                        p.removePotionEffect(PotionEffectType.BLINDNESS);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, refresh + 20, 0)); //+20 because the effect fades out in the last second

                        p.removePotionEffect(PotionEffectType.SLOW);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, refresh + 20, 2));
                    }
					
					for (Entity entity : p.getNearbyEntities(searchDistance, searchDistance, searchDistance))
						if (entity instanceof LivingEntity)
                            if (a)
                                showEffect(p, newRescale(p.getEyeLocation(), entity.getLocation(), viewDistance));
                            else
                                showEffect(p, rescale(p.getEyeLocation(), entity.getLocation(), searchDistance, viewDistance));
				}
			}
		}, 1, refresh);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			Player damagedPlayer = (Player) event.getEntity();
			sonarOff(damagedPlayer);
		}
	}

    private boolean a = true;
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {

            try {
                if (args[0].equalsIgnoreCase("on"))       sonarOn(((Player) sender));
                else if (args[0].equalsIgnoreCase("off")) sonarOff(((Player) sender));
                else if (args[0].equalsIgnoreCase("m"))   a = !a;
                else if (args[0].equalsIgnoreCase("a"))   sender.sendMessage("" + a);
                else sender.sendMessage("Unknown Argument!");
                return true;
            } catch (ArrayIndexOutOfBoundsException e) {}

            toggleSonar((Player) sender);

        } catch (ClassCastException e) {
            sender.sendMessage("Needs to be a player!");
        }
		
		return true;
	}
	
	public void sonarOn(Player player) {
		String s = player.getName();
		if (!SonarList.contains(s))
			SonarList.add(s);
	}
	
	public void sonarOff(Player player) {
		SonarList.remove(player.getName());
	}
	
	public boolean isUsingSonar(Player player) {
		return SonarList.contains(player.getName());
	}

    public void toggleSonar(Player player) {
        if (isUsingSonar(player))
            sonarOff(player);
        else
            sonarOn(player);
    }

    protected void showEffect(Player player, Location location){//Possible TODO: add more particles

        if(player.hasPermission("sonar.note")) ParticleEffect.NOTE.display(location, (float)0, (float)0, (float)0, (float)new Random().nextInt(23) + 1, 1, player);
        else ParticleEffect.FLAME.display(location, (float)0, (float)0, (float)0, (float)0, 1, player);

    }
    
    public void showWarningParticles(Player player, int amount, double distance) {
    	for (int i = 0; i < amount; i++) {
    		Vector vec = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5); //-0.5 because otherwise we only get vectors with positive coords
    		vec.normalize();
    		vec.multiply(distance);
    		vec.add(player.getEyeLocation().toVector());
    		
    		Location loc = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
    		ParticleEffect.RED_DUST.display(loc, (float)0, (float)0, (float)0, (float)0, 1, player);
    	}
    }
    
    public boolean areMonstersNearby(Player player, double radius) {
    	for (Entity entity : player.getNearbyEntities(radius, radius, radius))
    		if (isMonster(entity))
    			return true;
    	
    	return false;
    }
    
    public boolean isMonster(Entity entity) {
        return (entity instanceof Monster);//Should be cleaner and work with new monsters in future updates.
        /*
    	List<EntityType> hostileEntities = new ArrayList<EntityType>();
    	hostileEntities.add(EntityType.BLAZE);
    	hostileEntities.add(EntityType.CAVE_SPIDER);
    	hostileEntities.add(EntityType.CREEPER);
    	hostileEntities.add(EntityType.ENDER_DRAGON);
    	hostileEntities.add(EntityType.ENDERMAN);
    	hostileEntities.add(EntityType.GHAST);
    	hostileEntities.add(EntityType.GIANT);
    	hostileEntities.add(EntityType.MAGMA_CUBE);
    	hostileEntities.add(EntityType.PIG_ZOMBIE);
    	hostileEntities.add(EntityType.SILVERFISH);
    	hostileEntities.add(EntityType.SKELETON);
    	hostileEntities.add(EntityType.SLIME);
    	hostileEntities.add(EntityType.SPIDER);
    	hostileEntities.add(EntityType.WITCH);
    	hostileEntities.add(EntityType.WITHER);
    	hostileEntities.add(EntityType.ZOMBIE);
    	
    	if (hostileEntities.contains(entity.getType()))
    		return true;
    	
    	return false;*/
    }
	
	/*
	* center ~ Center location of circle(where sonar was activated)
	* point  ~ Location of entity
	* initialSize ~ Size of circle search range
	* newSize ~ Size of new circle (view range)
	*
	*  is = initalSize
	*  Ns = Newsize
	*  x = center.x
	*  ox = point.x
	*
	*  iS/Ns = s
	*  x - ox = dist
	*  dist / s = a
	*  x + a = nx
	*
	*  30/3 = 10
	*  0 - 20 = 20
	*  20 / 10 = 2
	*  0 + 2 = 2
	*/
    @Deprecated
	public Location rescale(Location center, Location point, Double initialSize, Double newSize) {
		double distanceX = center.getX() - point.getX();
		double distanceY = center.getY() - point.getY();
		double distanceZ = center.getZ() - point.getZ();
		double SizeScale = initialSize / newSize;
		
		double newX = center.getX() - (distanceX / SizeScale);
		double newY = center.getY() - (distanceY / SizeScale);
		double newZ = center.getZ() - (distanceZ / SizeScale);
		
		return new Location(center.getWorld(), newX, newY, newZ);
	}
	
	public Location newRescale(Location head, Location entity, double viewDistance) {
		Vector difference = entity.subtract(head).toVector();
		difference.normalize().multiply(viewDistance);
		
		Location particle = head.clone().add(difference);
		
		return particle;
	}

}
