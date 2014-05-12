package com.github.Gamecube762.Sonar;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.darkblade12.particleeffect.ParticleEffect;

/**
 * Created by Gamecube762 on 5/11/14.
 */
public class Main extends JavaPlugin {
	
	int refresh;//3 seconds
	double searchDistance, viewDistance;
	
	//static to help with /reloading
	protected static ArrayList<String> SonarList = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);		
		
		refresh = getConfig().getInt("refreshRate");
		searchDistance = getConfig().getDouble("searchDistance");
		viewDistance = getConfig().getDouble("viewDistance");
		
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			public void run() {
				for (String s : SonarList) {
					//todo: apply effects(slowness & blindness).
					Player p;
					try {
						p = Bukkit.getPlayer(s);
					} catch (NullPointerException e) {
						SonarList.remove(s);
						continue;
					}
					
					for (Entity entity : p.getNearbyEntities(searchDistance, searchDistance, searchDistance))
						if (entity instanceof LivingEntity)
							ParticleEffect.FLAME.display(newRescale(p.getEyeLocation(), entity.getLocation(), viewDistance), 0, 0, 0, 0, 1);
				}
			}
		}, 1, refresh);
		
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
			
			try {
				if (args[0].equalsIgnoreCase("on"))
					sonarOn(((Player) sender));
				else if (args[0].equalsIgnoreCase("off"))
					sonarOff(((Player) sender));
				else
					sender.sendMessage("Unknown Argument!");
				
				return true;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			Player player = ((Player) sender);
			
			if (isUsingSonar(player))
				sonarOff(player);
			else
				sonarOn(player);
			
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
	
	@Deprecated
	//Was just a test
	protected void createFlame(Location location) {
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 2);
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
