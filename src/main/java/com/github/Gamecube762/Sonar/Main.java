package com.github.Gamecube762.Sonar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
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
 */     //Possible Todo: make it optional to show all living_ents as one effect OR make effects customizable for living_ent types(animal, monster, player)
public class Main extends JavaPlugin implements Listener {
	
	int refresh, warningParticleAmount;
	double searchDistance, viewDistance, warningDistance;

	protected ArrayList<String> SonarList = new ArrayList<String>();
	
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
					Player player = Bukkit.getPlayer(s);
                    if (player == null) {
                        SonarList.remove(s);
                        continue;
                    }
					
					if (Utils.areMonstersNearby(player, warningDistance))
						showWarningParticles(player, warningParticleAmount, viewDistance);
					
                    if (!player.hasPermission("sonar.noDarkness")) {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, refresh + 20, 0)); //+20 because the effect fades out in the last second

                        player.removePotionEffect(PotionEffectType.SLOW);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, refresh + 20, 2));
                    }
					
					for (Entity entity : player.getNearbyEntities(searchDistance, searchDistance, searchDistance))
						if (entity instanceof LivingEntity) {

                            Location dl = (d) ? Utils.getEntityCenter((LivingEntity)entity) : entity.getLocation();

                            Location location = (a) ? newRescale(player.getEyeLocation(), dl, viewDistance) : rescale(player.getEyeLocation(), dl, searchDistance, viewDistance);
                            ParticleEffect particleEffect = (player.hasPermission("sonar.note")) ? ParticleEffect.NOTE : ParticleEffect.FLAME;

                            if (b)//this method cant be compressed like above since showEffect() doesn't return anything
                                showEffect(player, location, particleEffect);
                            else
                                showEffect(player, new Flame(entity, location, particleEffect) );
                        }
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

    private boolean
            a = true,// a = Math Method Toggle | default newRescale
            b = true,// b = between using new flame class or skipping
            c = true,// c = Use if methods or switch methods for showEffect()
	        d = true;// d = try to center the flame on entity(if not, flame appears at the feet)
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {

            try {
                if (args[0].equalsIgnoreCase("on"))       sonarOn(((Player) sender));
                else if (args[0].equalsIgnoreCase("off")) sonarOff(((Player) sender));
                else if (args[0].equalsIgnoreCase("a"))   sender.sendMessage("" + a);
                else if (args[0].equalsIgnoreCase("as"))  a = !a;
                else if (args[0].equalsIgnoreCase("b"))   sender.sendMessage("" + b);
                else if (args[0].equalsIgnoreCase("bs"))  b = !b;
                else if (args[0].equalsIgnoreCase("c"))   sender.sendMessage("" + c);
                else if (args[0].equalsIgnoreCase("cs"))  c = !c;
                else if (args[0].equalsIgnoreCase("d"))   sender.sendMessage("" + d);
                else if (args[0].equalsIgnoreCase("ds"))  d = !d;
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

    public ArrayList<String> getPlayersUsingSonar() {
        return SonarList;
    }

    public static ParticleEffect getPlayerParticleEffect(Player player) {
        if (player.hasPermission("sonar.note")) return ParticleEffect.NOTE;

        return ParticleEffect.FLAME;
    }

    protected /*static*/ void showEffect(Player player, Flame flame){
        showEffect(player, flame.getLocation(), flame.getParticleEffect());
    }
              //static temp disabled due to it using boolean c for testing
    protected /*static*/ void showEffect(Player player, Location loc, ParticleEffect particleEffect){//Switch is for custom options per effect, most effects will work on default

        if (c) {
            if (particleEffect == ParticleEffect.NOTE) particleEffect.display(loc, (float) 0, (float) 0, (float) 0, (float) new Random().nextInt(23) + 1, 1, player);
            else particleEffect.display(loc, (float) 0, (float) 0, (float) 0, (float) 0, 1, player);
        } else {
            switch (particleEffect) {
                case NOTE:
                    particleEffect.display(loc, (float) 0, (float) 0, (float) 0, (float) new Random().nextInt(23) + 1, 1, player);
                    break;
                default:
                    particleEffect.display(loc, (float) 0, (float) 0, (float) 0, (float) 0, 1, player);
                    break;
            }
        }

    }

    //Posible idea: make amount based on distance of closest monster
    public static void showWarningParticles(Player player, int amount, double distance) {
    	for (int i = 0; i < amount; i++) {
    		Vector vec = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5); //-0.5 because otherwise we only get vectors with positive coords
    		vec.normalize();
    		vec.multiply(distance);
    		vec.add(player.getEyeLocation().toVector());
    		
    		Location loc = new Location(player.getWorld(), vec.getX(), vec.getY(), vec.getZ());
    		ParticleEffect.RED_DUST.display(loc, (float)0, (float)0, (float)0, (float)0, 1, player);
    	}
    }

    @Deprecated //Moving to Utils.java
    public static boolean areMonstersNearby(Player player, double radius) {
    	for (Entity entity : player.getNearbyEntities(radius, radius, radius))
    		if (isMonster(entity))
    			return true;
    	
    	return false;
    }

    @Deprecated //Moving to Utils.java
    public static boolean isMonster(Entity entity) {
        List<EntityType> hostileEntities = new ArrayList<EntityType>();
    	hostileEntities.add(EntityType.GHAST);
    	hostileEntities.add(EntityType.MAGMA_CUBE);
    	hostileEntities.add(EntityType.SLIME); //These entities don't extend Monster so we have to check for them manually
    	
    	if (hostileEntities.contains(entity.getType()))
    		return true;
    	
    	return (entity instanceof Monster);//Should be cleaner and work with new monsters in future updates.
    }

    @Deprecated //Being removed
	public static Location rescale(Location center, Location point, Double initialSize, Double newSize) {
		double distanceX = center.getX() - point.getX();
		double distanceY = center.getY() - point.getY();
		double distanceZ = center.getZ() - point.getZ();
		double SizeScale = initialSize / newSize;
		
		double newX = center.getX() - (distanceX / SizeScale);
		double newY = center.getY() - (distanceY / SizeScale);
		double newZ = center.getZ() - (distanceZ / SizeScale);
		
		return new Location(center.getWorld(), newX, newY, newZ);
	}

    @Deprecated //Moving to Utils.java and renamed to "rescale"
	public static Location newRescale(Location head, Location entity, double viewDistance) {
		Vector difference = entity.subtract(head).toVector();
		difference.normalize().multiply(viewDistance);
		
		Location particle = head.clone().add(difference);
		
		return particle;
	}

    protected class Flame {
        Entity entity;
        Location location;
        ParticleEffect particleEffect;

        public Flame(Entity entity, Location location, ParticleEffect particleEffect) {
            this.entity = entity;
            this.location = location;
            this.particleEffect = particleEffect;
        }

        public Entity getEntity() {
            return entity;
        }

        public Location getLocation() {
            return location;
        }

        public ParticleEffect getParticleEffect() {
            return particleEffect;
        }
    }
}
