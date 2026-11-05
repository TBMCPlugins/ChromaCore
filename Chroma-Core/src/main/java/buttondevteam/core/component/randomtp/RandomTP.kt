package buttondevteam.core.component.randomtp

import buttondevteam.lib.chat.Command2.Subcommand
import buttondevteam.lib.chat.CommandClass
import buttondevteam.lib.chat.ICommand2MC
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.floor

// @formatter:off
@CommandClass(helpText = ["Random Teleport"
, "Teleport player to random location within world border. Every five players teleport to the same general area, and then a new general area is randomly selected for the next five players."
]) 
class RandomTP : ICommand2MC()
{
    private val radius = 70 //set how far apart the five teleport positions are
    
    private var world: World? = null
    private var border: WorldBorder? = null
    private var size = 0.0
    private var usableSize = 0.0
    private var borderCenterX = 0.0
    private var borderCenterZ = 0.0
    
    private var x = 0.0
    private var z = 0.0
    
    private var centerX = 0
    private var centerZ = 0
    private var centerY = 0
    private var northZ = 0
    private var southZ = 0
    private var eastX = 0
    private var westX = 0
    private var northY = 0
    private var southY = 0
    private var eastY = 0
    private var westY = 0
    
    private var centerGroundMaterial: Material? = null
    private var centerFeetMaterial: Material? = null
    private var centerHeadMaterial: Material? = null
    private var northGroundMaterial: Material? = null
    private var northFeetMaterial: Material? = null
    private var northHeadMaterial: Material? = null
    private var southGroundMaterial: Material? = null
    private var southFeetMaterial: Material? = null
    private var southHeadMaterial: Material? = null
    private var eastGroundMaterial: Material? = null
    private var eastFeetMaterial: Material? = null
    private var eastHeadMaterial: Material? = null
    private var westGroundMaterial: Material? = null
    private var westFeetMaterial: Material? = null
    private var westHeadMaterial: Material? = null
    
    private var center: Location? = null
    private var north: Location? = null
    private var south: Location? = null
    private var east: Location? = null
    private var west: Location? = null
    
    private var centerUsed = false
    private var northUsed = false
    private var southUsed = false
    private var eastUsed = false
    private var westUsed = false
    
    private val availableDirections = StringBuilder(5)
    private val chars = charArrayOf(1.toChar(), 2.toChar(), 3.toChar(), 4.toChar(), 5.toChar())
    private var dir = 0
    
     /*================================================================================================*/
    fun onEnable(component: RandomTPComponent) {
        world = Bukkit.getWorld("World")
        border = world!!.worldBorder
        component.log("Getting new location")
        if (border!!.size > 100000) component.logWarn("World border is wide, it may take a minute...")
        component.log("Success: " + newLocation())
    }
    
     /*================================================================================================*/
    @Subcommand fun def(sender: CommandSender, player: Player?): Boolean {
        if (sender.isOp) return rtp(player)
        
        else sender.sendMessage("§7 hmm, " + sender.name + "... " + sender.name + "... nope, no operator permissions.")
        
        return false
    }
    
     /*================================================================================================*/
    @Synchronized fun rtp(player: Player?): Boolean {
        if (player == null) return false //Pretend it rtp'd an imaginary player successfully
        
        
        		
		//if border has changed, or no positions available, find new location
        if ((centerUsed && northUsed && southUsed && eastUsed && westUsed) || 
        (borderCenterX != border!!.center.x || borderCenterZ != border!!.center.z || size != border!!.size)
        
        && !newLocation()) {
			//if unable to find new location, message player and return false
            player.sendMessage("§ccould not find a location in 10,000 attempts")
            player.sendMessage("§c (sorry bud... I did try!)")
            return false
        }
        
        		
		//randomly select one of the open positions and teleport the player there, then remove the position
        when (availableDirections.get(floor(Math.random() * availableDirections.length).toInt().also { dir = it })){1.toChar() -> {
                player.teleport(center!!)
                centerUsed = true}
            2.toChar() -> {
                player.teleport(north!!)
                northUsed = true}
            3.toChar() -> {
                player.teleport(south!!)
                southUsed = true}
            4.toChar() -> {
                player.teleport(east!!)
                eastUsed = true}
            5.toChar() -> {
                player.teleport(west!!)
                westUsed = true}}
        availableDirections.deleteCharAt(dir)
        
        		
		//imply that our server has a personality
        player.sendMessage("§7 *poof*")
        
        		
		//if all 5 positions have been teleported to, choose a new location
        if (centerUsed && northUsed && southUsed && eastUsed && westUsed) newLocation()
        
        return true
    }
    
     /*================================================================================================*/
    @Synchronized fun newLocation(): Boolean {
        size = border!!.size
        usableSize = size - (radius * 2)
        borderCenterX = border!!.center.x
        borderCenterZ = border!!.center.z
        
        		
		//maximum ten thousand attempts
        for (i in 0 .. 9999) {
			//choose an x and z inside the current world border, allowing a margin for the outer positions
            centerX = (floor((Math.random() - 0.5) * usableSize) + border!!.center.x).toInt()
            centerZ = (floor((Math.random() - 0.5) * usableSize) + border!!.center.z).toInt()


			//get center of block
            x = centerX + .5
            z = centerZ + .5


			//get other coordinates
            northZ = centerZ - radius
            southZ = centerZ + radius
            eastX = centerX + radius
            westX = centerX - radius

            centerY = world!!.getHighestBlockYAt(centerX, centerZ)
            northY = world!!.getHighestBlockYAt(centerX, northZ)
            southY = world!!.getHighestBlockYAt(centerX, southZ)
            eastY = world!!.getHighestBlockYAt(eastX, centerZ)
            westY = world!!.getHighestBlockYAt(westX, centerZ)


			//get materials for ground, feet-height and head-height blocks at each of the five positions
            centerGroundMaterial = world!!.getBlockAt(centerX, centerY - 1, centerZ).type
            northGroundMaterial = world!!.getBlockAt(centerX, northY - 1, northZ).type
            southGroundMaterial = world!!.getBlockAt(centerX, southY - 1, southZ).type
            eastGroundMaterial = world!!.getBlockAt(eastX, eastY - 1, centerZ).type
            westGroundMaterial = world!!.getBlockAt(westX, westY - 1, centerZ).type

            centerFeetMaterial = world!!.getBlockAt(centerX, centerY, centerZ).type
            northFeetMaterial = world!!.getBlockAt(centerX, northY, northZ).type
            southFeetMaterial = world!!.getBlockAt(centerX, southY, southZ).type
            eastFeetMaterial = world!!.getBlockAt(eastX, eastY, centerZ).type
            westFeetMaterial = world!!.getBlockAt(westX, westY, centerZ).type

            centerHeadMaterial = world!!.getBlockAt(centerX, centerY + 1, centerZ).type
            northHeadMaterial = world!!.getBlockAt(centerX, northY + 1, northZ).type
            southHeadMaterial = world!!.getBlockAt(centerX, southY + 1, southZ).type
            eastHeadMaterial = world!!.getBlockAt(eastX, eastY + 1, centerZ).type
            westHeadMaterial = world!!.getBlockAt(westX, westY + 1, centerZ).type


			//test that all five positions are on solid ground with air at head height
            if (centerHeadMaterial == Material.AIR && northHeadMaterial == Material.AIR && southHeadMaterial == Material.AIR && eastHeadMaterial == Material.AIR && westHeadMaterial == Material.AIR && centerFeetMaterial == Material.AIR && northFeetMaterial == Material.AIR && southFeetMaterial == Material.AIR && eastFeetMaterial == Material.AIR && westFeetMaterial == Material.AIR && centerGroundMaterial != Material.AIR && northGroundMaterial != Material.AIR && southGroundMaterial != Material.AIR && eastGroundMaterial != Material.AIR && westGroundMaterial != Material.AIR && centerGroundMaterial != Material.WATER && northGroundMaterial != Material.WATER && southGroundMaterial != Material.WATER && eastGroundMaterial != Material.WATER && westGroundMaterial != Material.WATER && centerGroundMaterial != Material.LAVA && northGroundMaterial != Material.LAVA && southGroundMaterial != Material.LAVA && eastGroundMaterial != Material.LAVA && westGroundMaterial != Material.LAVA) {
				//set new positions and reset
                center = Location(world, x, centerY.toDouble(), z)
                north = Location(world, x, northY.toDouble(), northZ + .5)
                south = Location(world, x, southY.toDouble(), southZ + .5)
                east = Location(world, eastX + .5, eastY.toDouble(), z)
                west = Location(world, westX + .5, westY.toDouble(), z)

                availableDirections.setLength(0)
                availableDirections.append(chars)

                westUsed = false
                eastUsed = false
                southUsed = false
                northUsed = false
                centerUsed = false

                return true
            }
        }
        westUsed = true
        eastUsed = true
        southUsed = true
        northUsed = true
        centerUsed = true
        
        return false
    }
}
