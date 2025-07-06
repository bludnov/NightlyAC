package wtf.sterfordovsky.nightlyanticheat.api.events.impl;

import org.bukkit.entity.Player;

public class CheckEvents {

    public static class CheckTriggerEvent extends Event {
        private final String checkName;
        private final Object[] data;
        private boolean cancelled = false;

        public CheckTriggerEvent(Player player, String checkName, Object... data) {
            super(player);
            this.checkName = checkName;
            this.data = data;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static class CheckPassEvent extends Event {
        private final String checkName;

        public CheckPassEvent(Player player, String checkName) {
            super(player);
            this.checkName = checkName;
        }
    }

    public static class PlayerDataClearEvent extends Event {
        private final String reason;

        public PlayerDataClearEvent(Player player, String reason) {
            super(player);
            this.reason = reason;
        }
    }
}