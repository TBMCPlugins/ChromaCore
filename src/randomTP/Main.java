package randomTP;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_11_R1.WorldBorder;

public class Main extends JavaPlugin
{
	private static final Random random = new Random();
	
	private static World		world;
	private static WorldBorder 	border;
	private static double 		size,
								borderCenterX,
								borderCenterZ;
	
	private static int 			x,z;
	private static final int 	radius = 70,
								diameter = radius * 2;
	
	private static Location		center,
								north,
								south,
								east,
								west;
		
	private static boolean 		centerUsed,
								northUsed, 
								southUsed, 
								eastUsed, 
								westUsed;
	
	private static StringBuffer availableDirections = new StringBuffer(5);
	private static int 			dir;
	
	/*================================================================================================*/
	
	public void onEnable()
	{
		world         = Bukkit.getWorld("World");
		border        = ((CraftWorld) world).getHandle().getWorldBorder();
		
		size          = border.getSize();
		borderCenterX = border.getCenterX();
		borderCenterZ = border.getCenterZ();
		
		getCommand("randomtp").setExecutor(this);
	}
	
	/*================================================================================================*/
		
	public boolean onCommand(CommandSender sender, Command label, String command, String[] args)
	{
		if (sender.isOp()) return rtp(Bukkit.getPlayer(args[0])); 
		
		else return false;
	}
	
	/*================================================================================================*/
	
	public synchronized boolean newLocation()
	{
		//MAXIMUM TEN THOUSAND ATTEMPTS
		for (int i = 0; i < 10000; i++)
		{
			//CHOOSE A RANDOM LOCATION WITHIN THE WORLDBORDER, ALLOWING SPACE FOR OUTER POSITIONS
			x = (int) Math.floor((random.nextDouble() - 0.5) * (size - diameter)) + center.getBlockX();
			z = (int) Math.floor((random.nextDouble() - 0.5) * (size - diameter)) + center.getBlockY();
			
			//CHECK THAT CENTER AND OUTER POSITIONS DO NOT HAVE WATER AT THEIR HIGHEST BLOCKS
			if (world.getHighestBlockAt( x          , z          ).getType() != Material.WATER &&
				world.getHighestBlockAt( x          , z - radius ).getType() != Material.WATER &&
				world.getHighestBlockAt( x          , z + radius ).getType() != Material.WATER &&
				world.getHighestBlockAt( x - radius , z          ).getType() != Material.WATER &&
				world.getHighestBlockAt( x + radius , z          ).getType() != Material.WATER)
			{
				//IF NEW LOCATION CHECKS OUT, RESET VALUES
				availableDirections.setCharAt(0, (char) 1);
				availableDirections.setCharAt(1, (char) 2);
				availableDirections.setCharAt(2, (char) 3);
				availableDirections.setCharAt(3, (char) 4);
				availableDirections.setCharAt(4, (char) 5);
				
				center = world.getHighestBlockAt( x          , z          ).getLocation();
				north  = world.getHighestBlockAt( x          , z - radius ).getLocation();
				south  = world.getHighestBlockAt( x          , z + radius ).getLocation();
				east   = world.getHighestBlockAt( x + radius , z          ).getLocation();
				west   = world.getHighestBlockAt( x - radius , z          ).getLocation();
				
				centerUsed = northUsed = southUsed = eastUsed = westUsed = false;
				
				return true;
			}
		}
		return false;
	}
	
	
	public synchronized boolean rtp(Player player)
	{
		if (player == null) 
			return false;
		
		//IF ALL POSITIONS USED, CHOOSE NEW LOCATION, AND IF NEW LOCATION FAILS RETURN FALSE
		if (centerUsed && northUsed && southUsed && eastUsed && westUsed && !newLocation()) 
			return false;
				
		//IF BORDER HAS CHANGED, CHOOSE NEW LOCATION, AND IF NEW LOCATION FAILS RETURN FALSE
		if ((borderCenterX != (borderCenterX = border.getCenterX()) ||  
			 borderCenterZ != (borderCenterZ = border.getCenterZ()) ||
			 size          != (size          = border.getSize()))        && !newLocation()) 
			return false;
		
		//CHOOSE ONE OF THE FIVE POSITIONS RANDOMLY AND TELEPORT THE PLAYER THERE, THEN REMOVE THAT POSITION
		switch(dir = availableDirections.charAt((int) Math.floor(random.nextDouble() * availableDirections.length())))
		{
			case (char) 1: player.teleport(center); centerUsed = true; break;
			case (char) 2: player.teleport(north ); northUsed  = true; break;
			case (char) 3: player.teleport(south ); southUsed  = true; break;
			case (char) 4: player.teleport(east  ); eastUsed   = true; break;
			case (char) 5: player.teleport(west  ); westUsed   = true; break;
		}
		availableDirections.deleteCharAt(dir); 
		
		//IF ALL POSITIONS USED, CHOOSE A NEW LOCATION FOR NEXT ROUND
		if (centerUsed && northUsed && southUsed && eastUsed && westUsed)
			newLocation();
		
		return true;
	}
}
