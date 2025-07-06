package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.BlockUtils;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;

public class KillAuraB extends Check implements Listener {
    
    public KillAuraB(ViolationManager violationManager) {
        super(violationManager, "KillAuraB");
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        CheckEvents.CheckTriggerEvent triggerEvent = new CheckEvents.CheckTriggerEvent(
            attacker, getName(), victim, event);
        violationManager.getEventManager().callCheckTriggerEvent(triggerEvent);
        
        if (triggerEvent.isCancelled()) return;
        
        if (BlockUtils.hasWallBetween(attacker, victim)) {
            flag(attacker, ColorUtils.ACCENT_COLOR + "попытка удара через блоки");
            event.setCancelled(true);
        } else {
            CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
            violationManager.getEventManager().callCheckPassEvent(passEvent);
        }
    }
    
    @Override
    public void cleanup() {
    }
    
    @Override
    public void removePlayerData(Player player) {
    }
}