package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.movement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;

public class MoveA extends Check implements Listener {
    
    private final int maxPositionPackets = 3;
    private final Map<Player, Integer> positionPacketCounts = new HashMap<>();
    
    public MoveA(ViolationManager violationManager) {
        super(violationManager, "MoveA");
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        positionPacketCounts.put(player, positionPacketCounts.getOrDefault(player, 0) + 1);
        
        Bukkit.getScheduler().runTaskLater(NightlyAC.getInstance(), () -> {
            positionPacketCounts.put(player, Math.max(0, positionPacketCounts.getOrDefault(player, 1) - 1));
        }, 2L);
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
        
        int packetCount = positionPacketCounts.getOrDefault(attacker, 0);
        
        if (packetCount > maxPositionPackets) {
            flag(attacker, ColorUtils.ACCENT_COLOR + "подозрительное количество пакетов позиции перед ударом (" +
                    ColorUtils.WARNING_COLOR + packetCount + 
                    ColorUtils.ACCENT_COLOR + ")");
            event.setCancelled(true);
        } else {
            CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
            violationManager.getEventManager().callCheckPassEvent(passEvent);
        }
    }
    
    @Override
    public void cleanup() {
        positionPacketCounts.keySet().removeIf(player -> !player.isOnline());
    }
    
    @Override
    public void removePlayerData(Player player) {
        positionPacketCounts.remove(player);
    }
}