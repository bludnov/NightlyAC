package wtf.sterfordovsky.nightlyanticheat.system.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.system.config.PunishmentConfig;
import wtf.sterfordovsky.nightlyanticheat.api.events.EventMngr;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.ViolationEvent;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;
import wtf.sterfordovsky.nightlyanticheat.api.utils.LogUtils;
import java.util.HashMap;
import java.util.Map;

public class ViolationManager {

    private final NightlyAC plugin;
    private final EventMngr eventManager;
    private final PunishmentConfig punishmentConfig;
    private final PunishmentManager punishmentManager;
    private final Map<Player, Integer> violationCounts = new HashMap<>();

    public ViolationManager(NightlyAC plugin) {
        this.plugin = plugin;
        this.eventManager = new EventMngr(plugin);
        this.punishmentConfig = new PunishmentConfig(plugin);
        this.punishmentConfig.saveDefaultConfig();
        this.punishmentManager = new PunishmentManager(plugin, punishmentConfig);
    }

    public void addViolation(Player player, String checkName, String reason) {
        int violations = violationCounts.getOrDefault(player, 0) + 1;
        violationCounts.put(player, violations);

        ViolationEvent event = new ViolationEvent(player, checkName, reason, violations);
        eventManager.callViolationEvent(event);

        if (!event.isCancelled()) {
            handleViolation(player, checkName, reason, violations);
        }
    }

    private void handleViolation(Player player, String checkName, String reason, int violations) {
        punishmentManager.getPlayerData(player).incrementViolations();

        String message = String.format(
                "%s %sИгрок %s%s %sнарушил правила [%s]: %s%s %s(§x§F§4§5§4§F§0%d§7)",
                ColorUtils.PREFIX,
                ColorUtils.TEXT_COLOR,
                ColorUtils.PLAYER_COLOR,
                player.getName(),
                ColorUtils.TEXT_COLOR,
                checkName,
                ColorUtils.VIOLATION_COLOR,
                reason,
                ColorUtils.TEXT_COLOR,
                violations
        );

        String consoleMessage = ChatColor.stripColor(message
                .replace("§x§F§4§5§4§F§0", "")
                .replace("§x§F§5§4§C§E§8", "")
                .replaceAll("§[0-9a-fk-or]", ""));

        plugin.getLogger().warning(consoleMessage);
        LogUtils.logToAdmins(message);

        punishmentManager.applyPunishment(player, violations);

        if (violations >= punishmentConfig.getKickThreshold()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                violationCounts.remove(player);
                plugin.getCheckManager().clearPlayerData(player);
            }, 20L);
        }
    }

    public void cleanup() {
        violationCounts.keySet().removeIf(player -> !player.isOnline());
        punishmentManager.cleanup();
    }

    public void removePlayerData(Player player) {
        violationCounts.remove(player);
        punishmentManager.removePlayerData(player);
    }

    public EventMngr getEventManager() {
        return eventManager;
    }

    public PunishmentConfig getPunishmentConfig() {
        return punishmentConfig;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public int getViolationCount(Player player) {
        return violationCounts.getOrDefault(player, 0);
    }

    public void shutdown() {
        punishmentManager.shutdown();
    }
}