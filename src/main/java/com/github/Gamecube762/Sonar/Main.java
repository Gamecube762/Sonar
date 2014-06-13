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
import org.bukkit.event.player.PlayerQuitEvent;
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

                    refreshPlayer(player, !player.hasPermission("sonar.noDarkness") );
				}
			}
		}, 1, refresh);
	}

    //Added to help prevent the player from needing to wait for the next refresh cycle
    public void refreshPlayer(Player player, boolean showEffects) {
        if (Utils.areMonstersNearby(player, warningDistance))
            showWarningParticles(player, warningParticleAmount, viewDistance);

        if (showEffects) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, refresh + 30, 0)); //+30 because the effect was fading just before the next refresh

            player.removePotionEffect(PotionEffectType.SLOW);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, refresh + 30, 5));

            player.removePotionEffect(PotionEffectType.JUMP);//Adding Negative Jump Boost for players using jumps to bypass slowness
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, refresh + 30, 128)); //Passing 127 on byte is like passing 0 when going down
        }

        for (Entity entity : player.getNearbyEntities(searchDistance, searchDistance, searchDistance))
            if (entity instanceof LivingEntity) {

                Location dl = (d) ? Utils.getEntityCenter((LivingEntity)entity) : entity.getLocation();

                Location location = Utils.rescale(player.getEyeLocation(), dl, viewDistance);

                ParticleEffect particleEffect = (player.hasPermission("sonar.note")) ? ParticleEffect.NOTE : ParticleEffect.FLAME;

                if (b)//this method cant be compressed like above since showEffect() doesn't return anything
                    showEffect(player, location, particleEffect);
                else
                    showEffect(player, new Flame(entity, location, particleEffect) );
            }
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

    public void onDisconnect(PlayerQuitEvent e) {
        sonarOff(e.getPlayer());
    }

    private boolean
            debug = false,
            //a = true,
            b = true,// b = between using new flame class or skipping
            c = true,// c = Use if methods or switch methods for showEffect()
	        d = true;// d = try to center the flame on entity(if not, flame appears at the feet)
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {

            try {
                if (args[0].equalsIgnoreCase("on"))       sonarOn(((Player) sender));
                else if (args[0].equalsIgnoreCase("off")) sonarOff(((Player) sender));

                //else if (args[0].equalsIgnoreCase("a") && debug)   sender.sendMessage("" + a);
                //else if (args[0].equalsIgnoreCase("as") && debug)  a = !a;
                else if (args[0].equalsIgnoreCase("b") && debug)   sender.sendMessage("" + b);
                else if (args[0].equalsIgnoreCase("bs") && debug)  b = !b;
                else if (args[0].equalsIgnoreCase("c") && debug)   sender.sendMessage("" + c);
                else if (args[0].equalsIgnoreCase("cs") && debug)  c = !c;
                else if (args[0].equalsIgnoreCase("d") && debug)   sender.sendMessage("" + d);
                else if (args[0].equalsIgnoreCase("ds") && debug)  d = !d;
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
        refreshPlayer(player, !player.hasPermission("sonar.noDarkness"));
	}
	
	public void sonarOff(Player player) {
		SonarList.remove(player.getName());

        if (!player.hasPermission("sonar.noDarkness")) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.JUMP);
        }
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
