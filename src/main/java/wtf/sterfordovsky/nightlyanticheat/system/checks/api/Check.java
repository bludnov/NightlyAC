package wtf.sterfordovsky.nightlyanticheat.system.checks.api;

import org.bukkit.entity.Player;
import wtf.sterfordovsky.nightlyanticheat.system.managers.ViolationManager;

public abstract class Check {
    
    protected final ViolationManager violationManager;
    private final String name;

    public Check(ViolationManager violationManager, String name) {
        this.violationManager = violationManager;
        this.name = name;
    }

    protected void flag(Player player, String message) {
        violationManager.addViolation(player, name, message);
    }

    public String getName() {
        return name;
    }

    public abstract void cleanup();

    public abstract void removePlayerData(Player player);
}