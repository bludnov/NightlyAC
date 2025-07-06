package wtf.sterfordovsky.nightlyanticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import wtf.sterfordovsky.nightlyanticheat.system.checks.api.CheckManager;
import wtf.sterfordovsky.nightlyanticheat.system.config.PunishmentConfig;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import wtf.sterfordovsky.nightlyanticheat.api.utils.ColorUtils;
import wtf.sterfordovsky.nightlyanticheat.api.utils.LogUtils;

public final class NightlyAC extends JavaPlugin {

    private static NightlyAC instance;
    private CheckManager checkManager;
    private ViolationManager violationManager;
    private PunishmentConfig punishmentConfig;

    @Override
    public void onEnable() {
        instance = this;
        
        displayStartupArt();
        
        LogUtils.info("§x§F§4§5§4§F§0[§x§F§7§3§B§D§7+§x§F§4§5§4§F§0] §x§F§7§3§B§D§7Загрузка конфигов...");
        
        punishmentConfig = new PunishmentConfig(this);
        punishmentConfig.saveDefaultConfig();
        
        LogUtils.info("§x§F§4§5§4§F§0[§x§F§7§3§B§D§7+§x§F§4§5§4§F§0] §x§F§7§3§B§D§7Инициализация античита...");
        
        violationManager = new ViolationManager(this);
        checkManager = new CheckManager(this, violationManager);
        
        LogUtils.info("§x§F§4§5§4§F§0[§x§F§7§3§B§D§7+§x§F§4§5§4§F§0] §x§F§7§3§B§D§7Запуск античита...");

        long enableTime = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            long timeTaken = System.currentTimeMillis() - enableTime;
            LogUtils.info("");
            LogUtils.info("§x§F§4§5§4§F§0[§x§F§A§2§A§C§6!§x§F§4§5§4§F§0] §x§F§7§3§B§D§7Античит загружен (за " + timeTaken + "мс)");
            LogUtils.info("§x§F§4§5§4§F§0[§x§F§7§3§B§D§7+§x§F§4§5§4§F§0] §x§F§7§3§B§D§7Система наказаний: Снижение урона с " + 
                punishmentConfig.getDamageReductionThreshold() + " нарушений, кик с " + 
                punishmentConfig.getKickThreshold() + " нарушений");
        }, 10L);
    }

    @Override
    public void onDisable() {
        if (checkManager != null) {
            checkManager.cleanup();
        }
        if (violationManager != null) {
            violationManager.shutdown();
        }
        LogUtils.logToAdmins(ColorUtils.PREFIX + " " + ColorUtils.TEXT_COLOR + "Плагин выключен");
    }

    private void displayStartupArt() {
        String[] startupArt = {
                "§x§F§4§5§4§F§0█▄░█ █ █▀▀ █░█ ▀█▀ █░░ █▄█  ▄▀█ █▀▀",
                "§x§F§5§4§C§E§8█░▀█ █ █▄█ █▀█ ░█░ █▄▄ ░█░  █▀█ █▄▄",
                "§x§F§7§3§B§D§7Running on §x§F§4§5§4§F§0" + Bukkit.getName() + " §x§F§7§3§B§D§7- §x§F§4§5§4§F§0" + Bukkit.getVersion(),
                ""
        };

        for (String line : startupArt) {
            getLogger().info(line);
        }
    }

    public static NightlyAC getInstance() {
        return instance;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }
}