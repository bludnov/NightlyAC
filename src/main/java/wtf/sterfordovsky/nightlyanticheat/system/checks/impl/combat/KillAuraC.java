package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;
import java.util.HashMap;
import java.util.Map;

public class KillAuraC extends Check implements Listener {
    
    private final long critCooldown = 500;
    private final double minFallDistance = 0.5;
    private final Map<Player, Long> lastCritTimes = new HashMap<>();
    
    public KillAuraC(ViolationManager violationManager) {
        super(violationManager, "KillAuraC");
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        CheckEvents.CheckTriggerEvent triggerEvent = new CheckEvents.CheckTriggerEvent(
            attacker, getName(), event);
        violationManager.getEventManager().callCheckTriggerEvent(triggerEvent);
        
        if (triggerEvent.isCancelled()) return;
        
        if (event.getDamage() > 7.0) {
            if (!canCrit(attacker)) {
                flag(attacker, ColorUtils.ACCENT_COLOR + "попытка нанесения крита с места");
                event.setCancelled(true);
                return;
            }
            lastCritTimes.put(attacker, System.currentTimeMillis());
        }
        
        CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
        violationManager.getEventManager().callCheckPassEvent(passEvent);
    }
    
    private boolean canCrit(Player player) {
        if (player.isInWater() || player.isInLava()) return false;
        if (player.isInsideVehicle()) return false;
        if (player.getFallDistance() < minFallDistance) return false;

        long now = System.currentTimeMillis();
        if (lastCritTimes.containsKey(player) && now - lastCritTimes.get(player) < critCooldown) {
            return false;
        }

        return !player.isOnGround() || isRecentlyJumped(player);
    }
    
    private boolean isRecentlyJumped(Player player) {
        return player.getVelocity().getY() > 0.1;
    }
    
    @Override
    public void cleanup() {
        long now = System.currentTimeMillis();
        lastCritTimes.entrySet().removeIf(entry -> now - entry.getValue() > 10000);
    }
    
    @Override
    public void removePlayerData(Player player) {
        lastCritTimes.remove(player);
    }
}