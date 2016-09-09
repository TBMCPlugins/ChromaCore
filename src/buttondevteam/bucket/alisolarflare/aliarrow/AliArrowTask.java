package buttondevteam.bucket.alisolarflare.aliarrow;

import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

import buttondevteam.bucket.MainPlugin;

public class AliArrowTask extends BukkitRunnable{
	MainPlugin plugin;
	Arrow arrow;
	
	
	
	public AliArrowTask(MainPlugin plugin, Arrow arrow){
		this.plugin = plugin;
		this.arrow = arrow;
	}
	
	@Override
	public void run() {
		arrow.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, arrow.getLocation(), 1);
		if (arrow.isOnGround() || arrow.isDead()){
			this.cancel();
		}
		
		
	}

}
