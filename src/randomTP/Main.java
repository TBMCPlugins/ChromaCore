package randomTP;

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
	private World			world;
	private WorldBorder 	border;
	private double 			size,
							borderCenterX,
							borderCenterZ;
	
	private int 			x,z;
	private final int 		radius = 70;
	
	private Location		center,
							north,
							south,
							east,
							west;
		
	private boolean 		centerUsed,
							northUsed, 
							southUsed, 
							eastUsed, 
							westUsed;
	
	private StringBuilder	availableDirections = new StringBuilder(5);
	private char[]			chars = {1,2,3,4,5};
	private int 			dir;
	
	/*================================================================================================*/
	
	public void onEnable()
	{
		getCommand("randomtp").setExecutor(this);
		
		world         = Bukkit.getWorld("World");
		border        = ((CraftWorld) world).getHandle().getWorldBorder();
		
		size          = border.getSize() - (radius * 2);
		borderCenterX = border.getCenterX();
		borderCenterZ = border.getCenterZ();
		
		newLocation();
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
			//CHOOSE A RANDOM AREA WITHIN THE WORLDBORDER, ALLOWING SPACE FOR OUTER POSITIONS
			x = (int) (Math.floor((Math.random() - 0.5) * size) + border.getCenterX());
			z = (int) (Math.floor((Math.random() - 0.5) * size) + border.getCenterZ());
			
			//CHECK THAT CENTER AND OUTER POSITIONS DO NOT HAVE WATER AT THEIR HIGHEST BLOCKS
			if (!world.getHighestBlockAt( x          , z          ).getType().equals(Material.WATER) &&
				!world.getHighestBlockAt( x          , z - radius ).getType().equals(Material.WATER) &&
				!world.getHighestBlockAt( x          , z + radius ).getType().equals(Material.WATER) &&
				!world.getHighestBlockAt( x - radius , z          ).getType().equals(Material.WATER) &&
				!world.getHighestBlockAt( x + radius , z          ).getType().equals(Material.WATER))
			{
				//IF NEW LOCATION IS VALID, RESET VALUES
				availableDirections.setLength(0);
				availableDirections.append(chars);
				
				center = world.getHighestBlockAt( x          , z          ).getLocation();
				north  = world.getHighestBlockAt( x          , z - radius ).getLocation();
				south  = world.getHighestBlockAt( x          , z + radius ).getLocation();
				east   = world.getHighestBlockAt( x + radius , z          ).getLocation();
				west   = world.getHighestBlockAt( x - radius , z          ).getLocation();
				
				centerUsed = northUsed = southUsed = eastUsed = westUsed = false;
				
				return true;
			}
		}
		centerUsed = northUsed = southUsed = eastUsed = westUsed = true;
		
		return false;
	}
	
	
	public synchronized boolean rtp(Player player)
	{
		if (player == null) 
			return false;
		
		//IF NO POSITIONS AVAILABLE, OR BORDER HAS CHANGED, FIND NEW LOCATION
		if (((centerUsed && northUsed && southUsed && eastUsed && westUsed) ||
				
			 borderCenterX != (borderCenterX = border.getCenterX()) ||  
			 borderCenterZ != (borderCenterZ = border.getCenterZ()) ||
			 size          != (size          = border.getSize()))
			
			&& !newLocation()) 
		{
			//RETURN FALSE AND MESSAGE PLAYER IF UNABLE TO FIND NEW LOCATION.
			player.sendMessage("could not find a location in 10,000 attempts");
			player.sendMessage("... sorry bud. I did try. 10,000 times.");
			return false;
		}
		
		//CHOOSE ONE OF THE FIVE POSITIONS RANDOMLY AND TELEPORT THE PLAYER THERE, THEN REMOVE THAT POSITION
		switch(availableDirections.charAt(dir = (int) Math.floor(Math.random() * availableDirections.length())))
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
