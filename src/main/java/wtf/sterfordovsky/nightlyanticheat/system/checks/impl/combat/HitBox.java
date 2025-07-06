package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;

import java.lang.reflect.Method;

public class HitBox extends Check implements Listener {

    private static final double MAX_ANGLE_DEGREES = 60.0;
    private static final double MAX_REACH_DISTANCE = 6.0;
    private static Method getBoundingBoxMethod;
    private static boolean reflectionInitialized = false;

    public HitBox(ViolationManager violationManager) {
        super(violationManager, "HitBox");
        initializeReflection();
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

        if (isHitboxViolation(attacker, victim)) {
            double angle = calculateAngleToTarget(attacker, victim);
            double distance = attacker.getEyeLocation().distance(victim.getLocation());
            
            flag(attacker, ColorUtils.ACCENT_COLOR + "удар мимо хитбокса (угол: " +
                    ColorUtils.WARNING_COLOR + String.format("%.1f°", angle) +
                    ColorUtils.ACCENT_COLOR + ", дистанция: " +
                    ColorUtils.WARNING_COLOR + String.format("%.2f", distance) +
                    ColorUtils.ACCENT_COLOR + ")");
            event.setCancelled(true);
        } else {
            CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
            violationManager.getEventManager().callCheckPassEvent(passEvent);
        }
    }

    private boolean isHitboxViolation(Player attacker, Player victim) {
        Vector eyeLocation = attacker.getEyeLocation().toVector();
        Vector direction = attacker.getLocation().getDirection();
        BoundingBox victimHitbox = getEntityBoundingBox(victim);
        if (victimHitbox == null) {
            victimHitbox = victim.getBoundingBox();
        }
        RayTraceResult rayTrace = victimHitbox.rayTrace(eyeLocation, direction, MAX_REACH_DISTANCE);
        
        if (rayTrace == null) {
            double angle = calculateAngleToTarget(attacker, victim);
            return angle > MAX_ANGLE_DEGREES;
        }
        double hitDistance = rayTrace.getHitPosition().distance(eyeLocation);
        double actualDistance = attacker.getEyeLocation().distance(victim.getEyeLocation());
        return hitDistance > actualDistance + 0.5;
    }

    private BoundingBox getEntityBoundingBox(Player player) {
        if (!reflectionInitialized) {
            return null;
        }
        
        try {
            return player.getBoundingBox();
        } catch (Exception e) {
            return getNMSBoundingBox(player);
        }
    }
    
    private BoundingBox getNMSBoundingBox(Player player) {
        try {
            Object nmsEntity = player.getClass().getMethod("getHandle").invoke(player);
            if (getBoundingBoxMethod == null) {
                getBoundingBoxMethod = nmsEntity.getClass().getMethod("getBoundingBox");
            }
            
            Object nmsBoundingBox = getBoundingBoxMethod.invoke(nmsEntity);
            double minX = (Double) nmsBoundingBox.getClass().getField("a").get(nmsBoundingBox);
            double minY = (Double) nmsBoundingBox.getClass().getField("b").get(nmsBoundingBox);
            double minZ = (Double) nmsBoundingBox.getClass().getField("c").get(nmsBoundingBox);
            double maxX = (Double) nmsBoundingBox.getClass().getField("d").get(nmsBoundingBox);
            double maxY = (Double) nmsBoundingBox.getClass().getField("e").get(nmsBoundingBox);
            double maxZ = (Double) nmsBoundingBox.getClass().getField("f").get(nmsBoundingBox);
            
            return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
            
        } catch (Exception e) {
            return null;
        }
    }

    private void initializeReflection() {
        try {
            reflectionInitialized = true;
        } catch (Exception e) {
            reflectionInitialized = false;
        }
    }

    private double calculateAngleToTarget(Player attacker, Player victim) {
        Vector attackerDirection = attacker.getLocation().getDirection().normalize();
        Vector toVictim = victim.getEyeLocation().toVector()
                .subtract(attacker.getEyeLocation().toVector()).normalize();

        double dotProduct = attackerDirection.dot(toVictim);
        dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
        
        return Math.toDegrees(Math.acos(dotProduct));
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void removePlayerData(Player player) {
    }
}