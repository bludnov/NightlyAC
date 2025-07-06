package wtf.sterfordovsky.nightlyanticheat.api.events.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public abstract class Event extends org.bukkit.event.Event {
    private static final HandlerList handlers = new HandlerList();
    protected final Player player;

    public Event(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}