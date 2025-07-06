package wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.Check;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;

public class KillAuraA extends Check implements Listener {

    private final double SURVIVAL_REACH = 3.0;
    private final double CREATIVE_REACH = 5.0;
    private final double REACH_TOLERANCE = 0.1;

    public KillAuraA(ViolationManager violationManager) {
        super(violationManager, "KillAuraA");
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

        double realDistance = calculateRealDistance(attacker, victim);
        double maxAllowedReach = getMaxReach(attacker) + REACH_TOLERANCE;

        if (realDistance > maxAllowedReach) {
            flag(attacker, ColorUtils.ACCENT_COLOR + "попытка удара с расстояния " +
                    ColorUtils.WARNING_COLOR + String.format("%.2f", realDistance) +
                    ColorUtils.ACCENT_COLOR + " блоков (макс: " +
                    ColorUtils.WARNING_COLOR + String.format("%.2f", maxAllowedReach) +
                    ColorUtils.ACCENT_COLOR + ")");
            event.setCancelled(true);
        } else {
            CheckEvents.CheckPassEvent passEvent = new CheckEvents.CheckPassEvent(attacker, getName());
            violationManager.getEventManager().callCheckPassEvent(passEvent);
        }
    }

    private double calculateRealDistance(Player attacker, Player victim) {
        Location attackerLoc = attacker.getLocation();
        Location victimLoc = victim.getLocation();
        BoundingBox attackerBox = attacker.getBoundingBox();
        BoundingBox victimBox = victim.getBoundingBox();
        double minX = Math.max(attackerBox.getMinX(), victimBox.getMinX());
        double maxX = Math.min(attackerBox.getMaxX(), victimBox.getMaxX());
        double minY = Math.max(attackerBox.getMinY(), victimBox.getMinY());
        double maxY = Math.min(attackerBox.getMaxY(), victimBox.getMaxY());
        double minZ = Math.max(attackerBox.getMinZ(), victimBox.getMinZ());
        double maxZ = Math.min(attackerBox.getMaxZ(), victimBox.getMaxZ());
        if (minX <= maxX && minY <= maxY && minZ <= maxZ) {
            return 0.0;
        }
        double dx = Math.max(0, Math.max(attackerBox.getMinX() - victimBox.getMaxX(),
                victimBox.getMinX() - attackerBox.getMaxX()));
        double dy = Math.max(0, Math.max(attackerBox.getMinY() - victimBox.getMaxY(),
                victimBox.getMinY() - attackerBox.getMaxY()));
        double dz = Math.max(0, Math.max(attackerBox.getMinZ() - victimBox.getMaxZ(),
                victimBox.getMinZ() - attackerBox.getMaxZ()));

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private double getMaxReach(Player player) {
        GameMode gameMode = player.getGameMode();

        switch (gameMode) {
            case CREATIVE:
            case SPECTATOR:
                return CREATIVE_REACH;
            case SURVIVAL:
            case ADVENTURE:
            default:
                return SURVIVAL_REACH;
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void removePlayerData(Player player) {
    }
}