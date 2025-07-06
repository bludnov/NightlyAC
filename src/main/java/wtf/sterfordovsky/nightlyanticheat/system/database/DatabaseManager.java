package wtf.sterfordovsky.nightlyanticheat.system.database;

import wtf.sterfordovsky.nightlyanticheat.NightlyAC;
import wtf.sterfordovsky.nightlyanticheat.api.data.PlayerPunishmentData;
import wtf.sterfordovsky.nightlyanticheat.api.utils.LogUtils;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DatabaseManager {
    
    private final NightlyAC plugin;
    private Connection connection;
    private final String dbPath;
    
    public DatabaseManager(NightlyAC plugin) {
        this.plugin = plugin;
        this.dbPath = plugin.getDataFolder().getAbsolutePath() + File.separator + "punishments.db";
        initDatabase();
    }
    
    private void initDatabase() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTables();
            LogUtils.info("Database initialized successfully");
        } catch (SQLException e) {
            LogUtils.warning("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void createTables() throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS player_punishments (
                uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                been_kicked INTEGER DEFAULT 0,
                damage_reduced INTEGER DEFAULT 0,
                last_violation_time INTEGER,
                total_violations INTEGER DEFAULT 0
            )
        """;
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTable);
        }
    }
    
    public void savePlayerData(PlayerPunishmentData data) {
        String sql = """
            INSERT OR REPLACE INTO player_punishments 
            (uuid, player_name, been_kicked, damage_reduced, last_violation_time, total_violations)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getPlayerId().toString());
            statement.setString(2, data.getPlayerName());
            statement.setInt(3, data.hasBeenKicked() ? 1 : 0);
            statement.setInt(4, data.isDamageReduced() ? 1 : 0);
            statement.setLong(5, data.getLastViolationTime());
            statement.setInt(6, data.getTotalViolations());
            statement.executeUpdate();
        } catch (SQLException e) {
            LogUtils.warning("Failed to save player data: " + e.getMessage());
        }
    }
    
    public PlayerPunishmentData loadPlayerData(UUID playerId, String playerName) {
        String sql = "SELECT * FROM player_punishments WHERE uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            ResultSet result = statement.executeQuery();
            
            if (result.next()) {
                PlayerPunishmentData data = new PlayerPunishmentData(playerId, playerName);
                data.setBeenKicked(result.getInt("been_kicked") == 1);
                data.setDamageReduced(result.getInt("damage_reduced") == 1);
                data.setLastViolationTime(result.getLong("last_violation_time"));
                data.setTotalViolations(result.getInt("total_violations"));
                return data;
            }
        } catch (SQLException e) {
            LogUtils.warning("Failed to load player data: " + e.getMessage());
        }
        
        return new PlayerPunishmentData(playerId, playerName);
    }
    
    public Map<UUID, PlayerPunishmentData> loadAllPlayerData() {
        Map<UUID, PlayerPunishmentData> data = new HashMap<>();
        String sql = "SELECT * FROM player_punishments";
        
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            
            while (result.next()) {
                UUID playerId = UUID.fromString(result.getString("uuid"));
                String playerName = result.getString("player_name");
                
                PlayerPunishmentData playerData = new PlayerPunishmentData(playerId, playerName);
                playerData.setBeenKicked(result.getInt("been_kicked") == 1);
                playerData.setDamageReduced(result.getInt("damage_reduced") == 1);
                playerData.setLastViolationTime(result.getLong("last_violation_time"));
                playerData.setTotalViolations(result.getInt("total_violations"));
                
                data.put(playerId, playerData);
            }
            
            LogUtils.info("Loaded " + data.size() + " player records from database");
        } catch (SQLException e) {
            LogUtils.warning("Failed to load all player data: " + e.getMessage());
        }
        
        return data;
    }
    
    public void removePlayerData(UUID playerId) {
        String sql = "DELETE FROM player_punishments WHERE uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            LogUtils.warning("Failed to remove player data: " + e.getMessage());
        }
    }
    
    public void cleanOldData(long maxAge) {
        String sql = "DELETE FROM player_punishments WHERE last_violation_time < ?";
        long cutoffTime = System.currentTimeMillis() - maxAge;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cutoffTime);
            int deleted = statement.executeUpdate();
            if (deleted > 0) {
                LogUtils.info("Cleaned " + deleted + " old player records");
            }
        } catch (SQLException e) {
            LogUtils.warning("Failed to clean old data: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LogUtils.warning("Failed to close database connection: " + e.getMessage());
        }
    }
}