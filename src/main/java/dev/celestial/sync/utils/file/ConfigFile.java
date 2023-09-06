package dev.celestial.sync.utils.file;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import dev.celestial.sync.utils.CC;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigFile {

    private final File configFile;
    private YamlConfiguration config;

    /**
     * The constructor to initialise the {@link ConfigFile} class
     *
     * @param plugin   The JavaPlugin object
     * @param fileName The name of the file to be initialised
     */
    public ConfigFile(JavaPlugin plugin, String fileName) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.configFile = new File(plugin.getDataFolder(), fileName);
        if (!this.configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    /**
     * Get a string from the config
     *
     * @param path The path to check
     * @return A string depending on whether the config path exists
     */
    public String getString(String path) {
        if (this.config.contains(path)) {
            return this.config.getString(path);
        }
        return "Invalid file path specified";
    }

    /**
     * Get a string list from a config
     *
     * @param path The path to check
     * @return A List/Singleton depending on the result
     */
    public List<String> getStringList(String path) {
        if (this.config.contains(path)) {
            return new ArrayList<>(this.config.getStringList(path));
        }
        return Collections.singletonList("Invalid string list path specified");
    }

    /**
     * Get a boolean from the config
     *
     * @param path The path to check
     * @return The result if the path exists, Default: false
     */
    public boolean getBoolean(String path) {
        if (this.config.contains(path)) {
            return this.config.getBoolean(path);
        }
        return false;
    }

    /**
     * Get an int from the config
     *
     * @param path The path to check
     * @return The result if the path exists. Default: 0
     */
    public int getInt(String path) {
        if (this.config.contains(path))
            return this.config.getInt(path);
        return 0;
    }

    /**
     * Get a double from the config
     *
     * @param path The path to check
     * @return The result if the path exists. Default: 0
     */
    public Double getDouble(String path) {
        if (this.config.contains(path))
            return this.config.getDouble(path);
        return 0D;
    }

    /**
     * Get a long from the config
     *
     * @param path The path to check
     * @return The result if the path exists. Default: 0
     */
    public Long getLong(String path) {
        if (this.config.contains(path)) {
            return this.config.getLong(path);
        }
        return 0L;
    }

    /**
     * Save the file
     */
    public void save() {
        try {
            this.config.save(this.configFile);
        } catch (Exception e) {
            CC.out("Failed to save file " + this.config);
        }
    }

    /**
     * Reload the file
     * @param save true/false - should we save the file
     */
    public void reload(boolean save) {
        if (save) {
            this.save();
        }

        this.config = YamlConfiguration.loadConfiguration(this.configFile);
    }

    /**
     * Save the {@link ConfigFile}
     *
     * @param path  The path to set
     * @param value The value to set in the path
     */
    public void set(String path, Object value) {
        this.config.set(path, value);
        this.save();
    }
    /**
     * Get an integer list from a config
     *
     * @param path The path to check
     * @return A List/Singleton depending on the result
     */
    public List<Integer> getIntegerList(String path) {
        if (this.config.contains(path)) {
            return new ArrayList<>(this.config.getIntegerList(path));
        }
        return Collections.singletonList(0);
    }
    /**
     * Getter for the {@link YamlConfiguration} object
     *
     * @return The {@link YamlConfiguration} instance
     */
    public YamlConfiguration getAsYaml() {
        return this.config;
    }
    /**
     * Get a configuration section from the config
     *
     * @param path The path to the configuration section
     * @return The ConfigurationSection if it exists, otherwise null
     */
    public ConfigurationSection getConfigurationSection(String path) {
        if (this.config.contains(path) && this.config.isConfigurationSection(path)) {
            return this.config.getConfigurationSection(path);
        }
        return null;
    }
}