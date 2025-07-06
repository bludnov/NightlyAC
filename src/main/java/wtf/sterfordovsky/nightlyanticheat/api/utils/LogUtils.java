package wtf.sterfordovsky.nightlyanticheat.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;

public class LogUtils {
    
    private static final String VIOLATIONS_PERMISSION = "nightlyac.violations";
    
    public static void info(String message) {
        NightlyAC.getInstance().getLogger().info(message);
    }
    
    public static void warning(String message) {
        NightlyAC.getInstance().getLogger().warning(message);
    }
    
    public static void logToAdmins(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission(VIOLATIONS_PERMISSION)) {
                admin.sendMessage(message);
            }
        }
    }
    
    private LogUtils() {
    }
}