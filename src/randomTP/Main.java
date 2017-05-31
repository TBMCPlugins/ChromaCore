package randomTP;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_11_R1.WorldBorder;

public class Main extends JavaPlugin
{
	private final int 		radius = 70; //set how far apart the five teleport positions are
	
	private CraftWorld		world;
	private WorldBorder 	border;
	private double 			size, 
							usableSize,
							borderCenterX,
							borderCenterZ,
							
							x,z;
							
	private int				centerX, centerZ, centerY, 
							northZ, southZ, eastX, westX,
							northY, southY, eastY, westY;
	
	private Material		centerGroundMaterial, centerFeetMaterial, centerHeadMaterial,
							northGroundMaterial,  northFeetMaterial,  northHeadMaterial,
							southGroundMaterial,  southFeetMaterial,  southHeadMaterial,
							eastGroundMaterial,   eastFeetMaterial,   eastHeadMaterial,
							westGroundMaterial,   westFeetMaterial,   westHeadMaterial;
							
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
		
		world = (CraftWorld) Bukkit.getWorld("World");
		border = world.getHandle().getWorldBorder();
		newLocation();
	}
	
	/*================================================================================================*/
		
	public boolean onCommand(CommandSender sender, Command label, String command, String[] args)
	{
		if (sender.isOp()) return rtp(Bukkit.getPlayer(args[0])); else return false;
	}
	
	/*================================================================================================*/
	
	public synchronized boolean rtp(Player player)
	{
		if (player == null) 
			return false;
		
		//if border has changed, or no positions available, find new location
		if ((centerUsed && northUsed && southUsed && eastUsed && westUsed) ||
			
			(borderCenterX != border.getCenterX() ||  
			 borderCenterZ != border.getCenterZ() ||
			 size          != border.getSize())
			
			&& !newLocation()) 
		{
			//message player and return false if unable to find new location
			player.sendMessage("§c could not find a location in 10,000 attempts");
			player.sendMessage("§c (sorry bud... I did try!)");
			return false;
		}
		
		//randomly select one of the open positions and teleport the player there, then remove the position
		switch(availableDirections.charAt(dir = (int) Math.floor(Math.random() * availableDirections.length())))
		{
			case (char) 1: player.teleport(center); centerUsed = true; break;
			case (char) 2: player.teleport(north ); northUsed  = true; break;
			case (char) 3: player.teleport(south ); southUsed  = true; break;
			case (char) 4: player.teleport(east  ); eastUsed   = true; break;
			case (char) 5: player.teleport(west  ); westUsed   = true; break;
		}
		availableDirections.deleteCharAt(dir);
		
		//imply that our server has a personality
		player.sendMessage("§7 *poof*");
		
		//if all 5 positions have been teleported to, choose a new location
		if (centerUsed && northUsed && southUsed && eastUsed && westUsed)
			newLocation();
		
		return true;
	}
	
	/*================================================================================================*/
	
	public synchronized boolean newLocation()
	{
		size          = border.getSize();
		usableSize    = size - (radius * 2);
		borderCenterX = border.getCenterX();
		borderCenterZ = border.getCenterZ();
		
		//maximum ten thousand attempts
		for (int i = 0; i < 10000; i++)
		{
			//choose an x and z inside the current world border, allowing a margin for the outer positions
			centerX = (int) (Math.floor((Math.random() - 0.5) * usableSize) + border.getCenterX());
			centerZ = (int) (Math.floor((Math.random() - 0.5) * usableSize) + border.getCenterZ());
			
			//get center of block
			x = centerX + .5;
			z = centerZ + .5;
			
			//get other coordinates
			northZ = centerZ - radius;		
			southZ = centerZ + radius;		
			eastX  = centerX + radius;		
			westX  = centerX - radius;		
			
			centerY = world.getHighestBlockYAt( centerX , centerZ );
			northY  = world.getHighestBlockYAt( centerX , northZ  );
			southY  = world.getHighestBlockYAt( centerX , southZ  );
			eastY   = world.getHighestBlockYAt( eastX   , centerZ );
			westY   = world.getHighestBlockYAt( westX   , centerZ );
			
			//get materials for ground, feet-height and head-height blocks at each of the five positions
			centerGroundMaterial = world.getBlockAt( centerX , centerY -1 , centerZ ).getType();
			northGroundMaterial  = world.getBlockAt( centerX , northY  -1 , northZ  ).getType();
			southGroundMaterial  = world.getBlockAt( centerX , southY  -1 , southZ  ).getType();
			eastGroundMaterial   = world.getBlockAt( eastX   , eastY   -1 , centerZ ).getType();
			westGroundMaterial   = world.getBlockAt( westX   , westY   -1 , centerZ ).getType();
			
			centerFeetMaterial   = world.getBlockAt( centerX , centerY    , centerZ ).getType();
			northFeetMaterial    = world.getBlockAt( centerX , northY     , northZ  ).getType();
			southFeetMaterial    = world.getBlockAt( centerX , southY     , southZ  ).getType();
			eastFeetMaterial     = world.getBlockAt( eastX   , eastY      , centerZ ).getType();
			westFeetMaterial     = world.getBlockAt( westX   , westY      , centerZ ).getType();
			
			centerHeadMaterial   = world.getBlockAt( centerX , centerY +1 , centerZ ).getType();
			northHeadMaterial    = world.getBlockAt( centerX , northY  +1 , northZ  ).getType();
			southHeadMaterial    = world.getBlockAt( centerX , southY  +1 , southZ  ).getType();
			eastHeadMaterial     = world.getBlockAt( eastX   , eastY   +1 , centerZ ).getType();
			westHeadMaterial     = world.getBlockAt( westX   , westY   +1 , centerZ ).getType();
			
			//test that all five positions are on solid ground with air at head height
			if (centerHeadMaterial   == Material.AIR &&
				northHeadMaterial    == Material.AIR &&
				southHeadMaterial    == Material.AIR &&
				eastHeadMaterial     == Material.AIR &&
				westHeadMaterial     == Material.AIR &&
				
				centerFeetMaterial   == Material.AIR &&
				northFeetMaterial    == Material.AIR &&
				southFeetMaterial    == Material.AIR &&
				eastFeetMaterial     == Material.AIR &&
				westFeetMaterial     == Material.AIR &&
				
				centerGroundMaterial != Material.AIR &&
				northGroundMaterial  != Material.AIR &&
				southGroundMaterial  != Material.AIR &&
				eastGroundMaterial   != Material.AIR &&
				westGroundMaterial   != Material.AIR &&
				
				centerGroundMaterial != Material.STATIONARY_WATER &&
				northGroundMaterial  != Material.STATIONARY_WATER &&
				southGroundMaterial  != Material.STATIONARY_WATER &&
				eastGroundMaterial   != Material.STATIONARY_WATER &&
				westGroundMaterial   != Material.STATIONARY_WATER &&
				
				centerGroundMaterial != Material.WATER &&
				northGroundMaterial  != Material.WATER &&
				southGroundMaterial  != Material.WATER &&
				eastGroundMaterial   != Material.WATER &&
				westGroundMaterial   != Material.WATER &&
				
				centerGroundMaterial != Material.STATIONARY_LAVA &&
				northGroundMaterial  != Material.STATIONARY_LAVA &&
				southGroundMaterial  != Material.STATIONARY_LAVA &&
				eastGroundMaterial   != Material.STATIONARY_LAVA &&
				westGroundMaterial   != Material.STATIONARY_LAVA &&
				
				centerGroundMaterial != Material.LAVA &&
				northGroundMaterial  != Material.LAVA &&
				southGroundMaterial  != Material.LAVA &&
				eastGroundMaterial   != Material.LAVA &&
				westGroundMaterial   != Material.LAVA)
			{
				//set new positions and reset
				center = new Location( world, x          , (double) centerY , z           );
				north  = new Location( world, x          , (double) northY  , northZ + .5 );
				south  = new Location( world, x          , (double) southY  , southZ + .5 );
				east   = new Location( world, eastX + .5 , (double) eastY   , z           );
				west   = new Location( world, westX + .5 , (double) westY   , z           );
				
				availableDirections.setLength(0);
				availableDirections.append(chars);
				
				centerUsed = 
				northUsed  = 
				southUsed  = 
				eastUsed   = 
				westUsed   = false;
				
				return true;
			}
		}
		centerUsed = 
		northUsed  = 
		southUsed  = 
		eastUsed   = 
		westUsed   = true;
		
		return false;
	}
}
