package wtf.sterfordovsky.nightlyanticheat.system.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.system.config.PunishmentConfig;
import wtf.sterfordovsky.nightlyanticheat.api.data.PlayerPunishmentData;
import wtf.sterfordovsky.nightlyanticheat.system.database.DatabaseManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;
import wtf.sterfordovsky.nightlyanticheat.api.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager implements Listener {

    private final NightlyAC plugin;
    private final PunishmentConfig config;
    private final DatabaseManager database;
    private final Map<UUID, PlayerPunishmentData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> damageReductionTasks = new HashMap<>();

    public PunishmentManager(NightlyAC plugin, PunishmentConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.database = new DatabaseManager(plugin);

        loadAllPlayerData();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void loadAllPlayerData() {
        playerData.putAll(database.loadAllPlayerData());
    }

    public PlayerPunishmentData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(),
                k -> database.loadPlayerData(player.getUniqueId(), player.getName()));
    }

    public void applyPunishment(Player player, int violations) {
        PlayerPunishmentData data = getPlayerData(player);

        if (violations >= config.getKickThreshold()) {
            if (data.hasBeenKicked()) {
                banPlayer(player);
            } else {
                kickPlayer(player);
                data.setBeenKicked(true);
                database.savePlayerData(data);
            }
        } else if (violations >= config.getDamageReductionThreshold()) {
            applyDamageReduction(player);
        }
    }

    private void applyDamageReduction(Player player) {
        UUID playerId = player.getUniqueId();

        BukkitTask existingTask = damageReductionTasks.get(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        PlayerPunishmentData data = getPlayerData(player);
        data.setDamageReduced(true);
        database.savePlayerData(data);

        String message = String.format(
                "%s %sИгрок %s%s %sполучил снижение урона на %d минут!",
                ColorUtils.PREFIX,
                ColorUtils.TEXT_COLOR,
                ColorUtils.PLAYER_COLOR,
                player.getName(),
                ColorUtils.WARNING_COLOR,
                config.getDamageReductionDuration()
        );

        LogUtils.logToAdmins(message);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            data.setDamageReduced(false);
            database.savePlayerData(data);
            damageReductionTasks.remove(playerId);
        }, config.getDamageReductionDuration() * 60L * 20L);

        damageReductionTasks.put(playerId, task);
    }

    private void kickPlayer(Player player) {
        String kickMessage = String.format(
                "%s %sИгрок %s%s %sбыл кикнут за читерство!",
                ColorUtils.PREFIX,
                ColorUtils.TEXT_COLOR,
                ColorUtils.PLAYER_COLOR,
                player.getName(),
                ColorUtils.WARNING_COLOR
        );

        LogUtils.logToAdmins(kickMessage);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "shame kick " + player.getName() + " " + config.getKickReason());
    }

    private void banPlayer(Player player) {
        String banMessage = String.format(
                "%s %sИгрок %s%s %sзабанен на %d дней за повторное читерство!",
                ColorUtils.PREFIX,
                ColorUtils.TEXT_COLOR,
                ColorUtils.PLAYER_COLOR,
                player.getName(),
                ColorUtils.WARNING_COLOR,
                config.getBanDurationDays()
        );

        LogUtils.logToAdmins(banMessage);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "shame ban " + player.getName());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        PlayerPunishmentData data = playerData.get(damager.getUniqueId());

        if (data != null && data.isDamageReduced()) {
            double currentDamage = event.getDamage();
            double reducedDamage = currentDamage / config.getDamageMultiplier();
            event.setDamage(reducedDamage);
        }
    }

    public void cleanup() {
        damageReductionTasks.values().forEach(BukkitTask::cancel);
        damageReductionTasks.clear();

        for (PlayerPunishmentData data : playerData.values()) {
            database.savePlayerData(data);
        }

        playerData.entrySet().removeIf(entry -> {
            Player player = Bukkit.getPlayer(entry.getKey());
            return player == null || !player.isOnline();
        });

        database.cleanOldData(24 * 60 * 60 * 1000L);
    }

    public void removePlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerPunishmentData data = playerData.get(playerId);

        if (data != null) {
            data.setDamageReduced(false);
            database.savePlayerData(data);
        }

        BukkitTask task = damageReductionTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void shutdown() {
        for (PlayerPunishmentData data : playerData.values()) {
            database.savePlayerData(data);
        }
        database.close();
    }
}