package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;
import java.util.HashMap;
import java.util.Map;

public class KillAuraD extends Check implements Listener {
    
    private final long minHitDelay = 250;
    private final double maxAngleChange = 90.0;
    private final Map<Player, Long> lastHitTimes = new HashMap<>();
    private final Map<Player, Location> lastHitLocations = new HashMap<>();
    
    public KillAuraD(ViolationManager violationManager) {
        super(violationManager, "KillAuraD");
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        Location hitLocation = victim.getLocation();
        long now = System.currentTimeMillis();
        CheckEvents.CheckTriggerEvent triggerEvent = new CheckEvents.CheckTriggerEvent(
            attacker, getName(), victim, event, hitLocation);
        violationManager.getEventManager().callCheckTriggerEvent(triggerEvent);
        
        if (triggerEvent.isCancelled()) return;
        
        boolean violation = false;
        
        if (lastHitTimes.containsKey(attacker)) {
            long timeSinceLastHit = now - lastHitTimes.get(attacker);
            
            if (timeSinceLastHit < minHitDelay) {
                flag(attacker, ColorUtils.ACCENT_COLOR + "подозрительно быстрый удар (" +
                        ColorUtils.WARNING_COLOR + timeSinceLastHit + ColorUtils.ACCENT_COLOR + "мс)");
                event.setCancelled(true);
                violation = true;
            }
            
            if (!violation && lastHitLocations.containsKey(attacker)) {
                Location lastHit = lastHitLocations.get(attacker);
                double angleChange = calculateAngleChange(attacker, lastHit, hitLocation);
                
                if (angleChange > maxAngleChange && timeSinceLastHit < 500) {
                    flag(attacker, ColorUtils.ACCENT_COLOR + "подозрительно резкое изменение направления атаки (" +
                            ColorUtils.WARNING_COLOR + String.format("%.1f", angleChange) + 
                            ColorUtils.ACCENT_COLOR + "°)");
                    event.setCancelled(true);
                    violation = true;
                }
            }
        }
        
        if (!violation) {
            CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
            violationManager.getEventManager().callCheckPassEvent(passEvent);
        }
        
        lastHitTimes.put(attacker, now);
        lastHitLocations.put(attacker, hitLocation.clone());
    }
    
    private double calculateAngleChange(Player attacker, Location lastHit, Location currentHit) {
        Vector lastDirection = lastHit.toVector().subtract(attacker.getLocation().toVector());
        Vector currentDirection = currentHit.toVector().subtract(attacker.getLocation().toVector());
        return Math.toDegrees(lastDirection.angle(currentDirection));
    }
    
    @Override
    public void cleanup() {
        long now = System.currentTimeMillis();
        lastHitTimes.entrySet().removeIf(entry -> now - entry.getValue() > 5000);
        lastHitLocations.keySet().removeIf(player -> !player.isOnline());
    }
    
    @Override
    public void removePlayerData(Player player) {
        lastHitTimes.remove(player);
        lastHitLocations.remove(player);
    }
}