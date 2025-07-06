package wtf.sterfordovsky.nightlyanticheat.system.config;

import org.bukkit.configuration.file.FileConfiguration;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;

public class PunishmentConfig {

    private final NightlyAC plugin;
    private FileConfiguration config;

    private int damageReductionThreshold;
    private int kickThreshold;
    private double damageMultiplier;
    private int damageReductionDuration;
    private int banDurationDays;
    private String kickReason;
    private String banReason;

    public PunishmentConfig(NightlyAC plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        damageReductionThreshold = config.getInt("punishments.damage-reduction.threshold", 10);
        kickThreshold = config.getInt("punishments.kick.threshold", 15);
        damageMultiplier = config.getDouble("punishments.damage-reduction.multiplier", 1.5);
        damageReductionDuration = config.getInt("punishments.damage-reduction.duration-minutes", 5);
        banDurationDays = config.getInt("punishments.ban.duration-days", 15);
        kickReason = config.getString("punishments.kick.reason", "[NAC] Пункт АЧ");
        banReason = config.getString("punishments.ban.reason", "[NAC] АнтиЧит");
    }

    public void saveDefaultConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        config = plugin.getConfig();

        config.addDefault("punishments.damage-reduction.threshold", 10);
        config.addDefault("punishments.damage-reduction.multiplier", 1.5);
        config.addDefault("punishments.damage-reduction.duration-minutes", 5);
        config.addDefault("punishments.kick.threshold", 15);
        config.addDefault("punishments.kick.reason", "[NAC] Пункт АЧ");
        config.addDefault("punishments.ban.duration-days", 15);
        config.addDefault("punishments.ban.reason", "[NAC] АнтиЧит");

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public int getDamageReductionThreshold() {
        return damageReductionThreshold;
    }

    public int getKickThreshold() {
        return kickThreshold;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getDamageReductionDuration() {
        return damageReductionDuration;
    }

    public int getBanDurationDays() {
        return banDurationDays;
    }

    public String getKickReason() {
        return kickReason;
    }

    public String getBanReason() {
        return banReason;
    }
}