package wtf.sterfordovsky.nightlyanticheat.api.data;

import java.io.Serializable;
import java.util.UUID;

public class PlayerPunishmentData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID playerId;
    private final String playerName;
    private boolean beenKicked;
    private boolean damageReduced;
    private long lastViolationTime;
    private int totalViolations;

    public PlayerPunishmentData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.beenKicked = false;
        this.damageReduced = false;
        this.lastViolationTime = System.currentTimeMillis();
        this.totalViolations = 0;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean hasBeenKicked() {
        return beenKicked;
    }

    public void setBeenKicked(boolean beenKicked) {
        this.beenKicked = beenKicked;
    }

    public boolean isDamageReduced() {
        return damageReduced;
    }

    public void setDamageReduced(boolean damageReduced) {
        this.damageReduced = damageReduced;
    }

    public long getLastViolationTime() {
        return lastViolationTime;
    }

    public void setLastViolationTime(long lastViolationTime) {
        this.lastViolationTime = lastViolationTime;
    }

    public int getTotalViolations() {
        return totalViolations;
    }

    public void setTotalViolations(int totalViolations) {
        this.totalViolations = totalViolations;
    }

    public void incrementViolations() {
        this.totalViolations++;
        this.lastViolationTime = System.currentTimeMillis();
    }

    public void resetViolations() {
        this.totalViolations = 0;
        this.lastViolationTime = System.currentTimeMillis();
    }

    public boolean shouldResetData(long maxIdleTime) {
        return System.currentTimeMillis() - lastViolationTime > maxIdleTime;
    }
}