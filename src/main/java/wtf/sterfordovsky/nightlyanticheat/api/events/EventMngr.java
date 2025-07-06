package wtf.sterfordovsky.nightlyanticheat.api.events;

import org.bukkit.Bukkit;
import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.api.events.impl.*;

public class EventMngr {
    
    private final NightlyAC plugin;

    public EventMngr(NightlyAC plugin) {
        this.plugin = plugin;
    }

    public ViolationEvent callViolationEvent(ViolationEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public CheckEvents.CheckTriggerEvent callCheckTriggerEvent(CheckEvents.CheckTriggerEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public CheckEvents.CheckPassEvent callCheckPassEvent(CheckEvents.CheckPassEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public CheckEvents.PlayerDataClearEvent callPlayerDataClearEvent(CheckEvents.PlayerDataClearEvent event) {
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}