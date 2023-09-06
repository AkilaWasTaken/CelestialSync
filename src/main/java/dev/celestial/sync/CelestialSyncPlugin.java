package dev.celestial.sync;

import co.aikar.commands.PaperCommandManager;
import dev.celestial.sync.cmd.CmdSync;
import dev.celestial.sync.database.DatabaseHandler;
import dev.celestial.sync.discord.DiscordConnector;
import dev.celestial.sync.managers.DatabaseManager;
import dev.celestial.sync.utils.CC;
import dev.celestial.sync.utils.file.ConfigFile;
import org.bukkit.plugin.java.JavaPlugin;

public final class CelestialSyncPlugin extends JavaPlugin {
    private static CelestialSyncPlugin instance;
    public static DatabaseHandler databaseHandler;
    private ConfigFile settingsFile;
    private static ConfigFile discordFile;
    private static ConfigFile minecraftFile;

    private static DiscordConnector discordConnector;
    @Override
    public void onEnable() {
        instance = this;
        CC.sendPluginStartupMessage();
        settingsFile = new ConfigFile(this, "settings.yml");
        databaseHandler = new DatabaseHandler(this, settingsFile);
        getDiscord().connect();
        registerCommands();
    }
    public void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CmdSync(this, minecraftFile, DatabaseManager.getDataSource()));
    }
    public synchronized DiscordConnector getDiscord() {
        if (discordConnector == null) {
            discordConnector = new DiscordConnector(this, discordFile, DatabaseManager.getDataSource());
        }
        return discordConnector;
    }
    @Override
    public void onDisable() {
        if (getDiscord().getJDA() != null) {
            getDiscord().disconnect();
        }
    }
}