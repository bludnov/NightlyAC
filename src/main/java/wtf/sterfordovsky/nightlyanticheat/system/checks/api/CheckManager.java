package wtf.sterfordovsky.nightlyanticheat.system.checks.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.system.checks.impl.combat.*;
import wtf.sterfordovsky.nightlyanticheat.system.checks.impl.movement.MoveA;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.CheckEvents;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;
import java.util.ArrayList;
import java.util.List;

public class CheckManager implements Listener {

    private final NightlyAC plugin;
    private final ViolationManager violationManager;
    private final List<Check> checks = new ArrayList<>();

    public CheckManager(NightlyAC plugin, ViolationManager violationManager) {
        this.plugin = plugin;
        this.violationManager = violationManager;

        initializeChecks();
        registerEvents();
        startCleanupTask();
    }

    private void initializeChecks() {
        checks.add(new KillAuraA(violationManager));
        checks.add(new KillAuraB(violationManager));
        checks.add(new KillAuraC(violationManager));
        checks.add(new KillAuraD(violationManager));
        checks.add(new HitBox(violationManager));
        checks.add(new MoveA(violationManager));
    }

    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        for (Check check : checks) {
            if (check instanceof Listener) {
                plugin.getServer().getPluginManager().registerEvents((Listener) check, plugin);
            }
        }
    }

    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Check check : checks) {
                check.cleanup();
            }
            violationManager.cleanup();
        }, 1200L, 1200L);
    }

    public void clearPlayerData(Player player) {
        for (Check check : checks) {
            check.removePlayerData(player);
        }
        violationManager.removePlayerData(player);
        CheckEvents.PlayerDataClearEvent event = new CheckEvents.PlayerDataClearEvent(player, "Player disconnect");
        violationManager.getEventManager().callPlayerDataClearEvent(event);
    }

    public void cleanup() {
        for (Check check : checks) {
            check.cleanup();
        }
        checks.clear();
    }
}