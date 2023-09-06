package dev.celestial.sync.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.celestial.sync.database.DatabaseHandler;

public class DatabaseManager {
    private static HikariDataSource hikariDataSource;

    public DatabaseManager(DatabaseHandler databaseHandler) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + databaseHandler.getAddress() + ":" + databaseHandler.getPort() + "/" + databaseHandler.getDatabase());
        hikariConfig.setUsername(databaseHandler.getUsername());
        hikariConfig.setPassword(databaseHandler.getPassword());
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("useUnicode", true);
        hikariConfig.addDataSourceProperty("useSSL", databaseHandler.isSsl());
        hikariConfig.setMaximumPoolSize(10);

        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static HikariDataSource getDataSource() {
        return hikariDataSource;
    }
}