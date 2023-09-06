package dev.celestial.sync.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.celestial.sync.managers.DatabaseManager;
import dev.celestial.sync.utils.file.ConfigFile;
import dev.celestial.sync.CelestialSyncPlugin;
import dev.celestial.sync.database.profile.CodeProfile;
import dev.celestial.sync.database.profile.VerifiedProfile;
import lombok.Getter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Getter
public class DatabaseHandler {

    private HikariDataSource hikariDataSource;
    private final Executor hikariExecutor = Executors.newFixedThreadPool(2);
    private ConnectionSource connectionSource;
    private Dao<CodeProfile, Integer> codeProfileDao;
    private Dao<VerifiedProfile, Integer> verifiedProfileDao;
    private CelestialSyncPlugin plugin;
    private String address, database, username, password;
    private int port;
    private boolean ssl, dbEnabled;
    private ConfigFile settingsFile;

    public DatabaseHandler(CelestialSyncPlugin plugin, ConfigFile settingsFile) {
        this.plugin = plugin;
        this.settingsFile = settingsFile;
        this.loadCredentials();

        HikariConfig hikariConfig = new HikariConfig();
        if (this.dbEnabled) {
            hikariConfig.setJdbcUrl("jdbc:mysql://" + this.address + ":" + this.port + "/" + this.database);
        }
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("useUnicode", true);
        hikariConfig.addDataSourceProperty("useSSL", this.ssl);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setPoolName("Sync-Pool");
        DatabaseManager databaseManager = new DatabaseManager(this);
        this.hikariDataSource = databaseManager.getDataSource();

        try {
            this.connectionSource = new DataSourceConnectionSource(this.hikariDataSource, hikariConfig.getJdbcUrl());
            this.codeProfileDao = DaoManager.createDao(this.connectionSource, CodeProfile.class);
            this.verifiedProfileDao = DaoManager.createDao(this.connectionSource, VerifiedProfile.class);
            TableUtils.createTableIfNotExists(this.connectionSource, CodeProfile.class);
            TableUtils.createTableIfNotExists(this.connectionSource, VerifiedProfile.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCredentials() {
        this.dbEnabled = true;
        this.address = settingsFile.getString("DATABASE.MYSQL.HOST");
        this.port = settingsFile.getInt("DATABASE.MYSQL.PORT");
        this.database = settingsFile.getString("DATABASE.MYSQL.DATABASE");
        this.username = settingsFile.getString("DATABASE.MYSQL.USERNAME");
        this.password = settingsFile.getString("DATABASE.MYSQL.PASSWORD");
        this.ssl = settingsFile.getBoolean("DATABASE.MYSQL.SSL");
    }

    public void disconnect() throws Exception {
        if (this.codeProfileDao != null) {
            this.codeProfileDao.closeLastIterator();
            this.codeProfileDao = null;
        }
        if (this.connectionSource != null) {
            this.connectionSource.closeQuietly();
            this.connectionSource = null;
        }
        if (this.hikariDataSource != null) {
            this.hikariDataSource.close();
            this.hikariDataSource = null;
        }
    }
}