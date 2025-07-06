package wtf.sterfordovsky.nightlyanticheat.api.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockUtils {
    
    private static final double WALL_CHECK_PRECISION = 0.5;
    
    public static boolean hasWallBetween(Player attacker, Player victim) {
        Location from = attacker.getEyeLocation();
        Location to = victim.getEyeLocation();
        Vector direction = to.toVector().subtract(from.toVector()).normalize();

        for (double d = 0; d < from.distance(to); d += WALL_CHECK_PRECISION) {
            Vector point = direction.clone().multiply(d);
            Location checkLoc = from.clone().add(point);

            Block block = checkLoc.getBlock();
            if (block.getType().isSolid() && !isTransparent(block.getType())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isTransparent(Material material) {
        return material == Material.GLASS ||
                material == Material.SLIME_BLOCK ||
                material == Material.IRON_BARS ||
                material == Material.CHAIN ||
                material == Material.LADDER ||
                material == Material.VINE ||
                material.toString().contains("FENCE") ||
                material.toString().contains("LEAVES");
    }
    
    private BlockUtils()
    {
    }
}