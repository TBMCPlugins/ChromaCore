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
							borderCenterZ;
	
	private int 			x,z,
							centerY, northY, southY, eastY, westY,
							         northZ, southZ, eastX, westX;
	
	private Material		centerGroundMaterial, centerHeadMaterial,
							northGroundMaterial,  northHeadMaterial,
							southGroundMaterial,  southHeadMaterial,
							eastGroundMaterial,   eastHeadMaterial,
							westGroundMaterial,   westHeadMaterial;
							
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
		world  = (CraftWorld) Bukkit.getWorld("World");
		border = world.getHandle().getWorldBorder();
		
		newLocation();
		
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
		size          = border.getSize();
		usableSize    = size - (radius * 2);
		borderCenterX = border.getCenterX();
		borderCenterZ = border.getCenterZ();
		
		//MAXIMUM TEN THOUSAND ATTEMPTS
		for (int i = 0; i < 10000; i++)
		{
			//RANDOMLY CHOOSE AN X AND Z WITHIN WORLD BORDER
			x = (int) (Math.floor((Math.random() - 0.5) * usableSize) + border.getCenterX());
			z = (int) (Math.floor((Math.random() - 0.5) * usableSize) + border.getCenterZ());
			
			//GET OTHER COORDINATES
										centerY = world.getHighestBlockYAt( x     , z      );
			northZ = z - radius;		northY  = world.getHighestBlockYAt( x     , northZ );
			southZ = z + radius;		southY  = world.getHighestBlockYAt( x     , southZ );
			eastX  = x + radius;		eastY   = world.getHighestBlockYAt( eastX , z      );
			westX  = x - radius;		westY   = world.getHighestBlockYAt( westX , z      );
			
			//GET MATERIALS FOR GROUND AND HEAD-HEIGHT BLOCKS AT EACH POSITION
			centerGroundMaterial = world.getBlockAt( x     , centerY -1 , z      ).getType();
			northGroundMaterial  = world.getBlockAt( x     , northY  -1 , northZ ).getType();
			southGroundMaterial  = world.getBlockAt( x     , southY  -1 , southZ ).getType();
			eastGroundMaterial   = world.getBlockAt( eastX , eastY   -1 , z      ).getType();
			westGroundMaterial   = world.getBlockAt( westX , westY   -1 , z      ).getType();
			
			centerHeadMaterial   = world.getBlockAt( x     , centerY +1 , z      ).getType();
			northHeadMaterial    = world.getBlockAt( x     , northY  +1 , northZ ).getType();
			southHeadMaterial    = world.getBlockAt( x     , southY  +1 , southZ ).getType();
			eastHeadMaterial     = world.getBlockAt( eastX , eastY   +1 , z      ).getType();
			westHeadMaterial     = world.getBlockAt( westX , westY   +1 , z      ).getType();
			
			//CONFIRM THAT ALL FIVE POSITIONS ARE ON SOLID GROUND WITH AIR AT HEAD HEIGHT
			if (centerHeadMaterial == Material.AIR &&
				northHeadMaterial  == Material.AIR &&
				southHeadMaterial  == Material.AIR &&
				eastHeadMaterial   == Material.AIR &&
				westHeadMaterial   == Material.AIR &&
					
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
				//IF LOCATION VALID, SET NEW POSITIONS AND RESET TRACKING VARIABLES
				center = world.getBlockAt( x     , centerY , z      ).getLocation();
				north  = world.getBlockAt( x     , northY  , northZ ).getLocation();
				south  = world.getBlockAt( x     , southY  , southZ ).getLocation();
				east   = world.getBlockAt( eastX , eastY   , z      ).getLocation();
				west   = world.getBlockAt( westX , westY   , z      ).getLocation();
				
				availableDirections.setLength(0);
				availableDirections.append(chars);
				
				centerUsed = northUsed = southUsed = eastUsed = westUsed = false;
				
				return true;
			}
		}
		centerUsed = northUsed = southUsed = eastUsed = westUsed = true;
		
		return false;
	}
	
	/*================================================================================================*/
	
	public synchronized boolean rtp(Player player)
	{
		if (player == null) 
			return false;
		
		//IF BORDER HAS CHANGED, OR NO POSITIONS AVAILABLE, FIND NEW LOCATION
		if ((centerUsed && northUsed && southUsed && eastUsed && westUsed) ||
			
			(borderCenterX != border.getCenterX() ||  
			 borderCenterZ != border.getCenterZ() ||
			 size          != border.getSize())
			
			&& !newLocation()) 
		{
			//MESSAGE PLAYER AND RETURN FALSE IF UNABLE TO FIND NEW LOCATION.
			player.sendMessage("could not find location in 10,000 attempts");
			player.sendMessage("... sorry bud. I did try.");
			return false;
		}
		
		//RANDOMLY SELECT ONE OF THE OPEN POSITIONS AND TELEPORT THE PLAYER THERE. THEN, REMOVE THE POSITION
		switch(availableDirections.charAt(dir = (int) Math.floor(Math.random() * availableDirections.length())))
		{
			case (char) 1: player.teleport(center); centerUsed = true; break;
			case (char) 2: player.teleport(north ); northUsed  = true; break;
			case (char) 3: player.teleport(south ); southUsed  = true; break;
			case (char) 4: player.teleport(east  ); eastUsed   = true; break;
			case (char) 5: player.teleport(west  ); westUsed   = true; break;
		}
		availableDirections.deleteCharAt(dir);
		
		//IF ALL 5 POSITIONS HAVE BEEN TELEPORTED TO, CHOOSE NEW LOCATION
		if (centerUsed && northUsed && southUsed && eastUsed && westUsed)
			newLocation();
		
		return true;
	}
}
