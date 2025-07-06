package wtf.sterfordovsky.nightlyanticheat.api.events.impl;

import org.bukkit.entity.Player;

public class ViolationEvent extends Event {
    
    private final String checkName;
    private final String message;
    private final int vl;
    private boolean cancelled = false;

    public ViolationEvent(Player player, String checkName, String message, int vl) {
        super(player);
        this.checkName = checkName;
        this.message = message;
        this.vl = vl;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}