package dev.celestial.sync.discord;

import com.zaxxer.hikari.HikariConfig;
import dev.celestial.sync.discord.commands.SyncCommand;
import dev.celestial.sync.model.IDiscordModel;
import dev.celestial.sync.utils.file.ConfigFile;
import dev.celestial.sync.CelestialSyncPlugin;
import dev.celestial.sync.discord.commands.UnSyncCommand;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.bukkit.plugin.java.JavaPlugin;

import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class DiscordConnector implements IDiscordModel {

    private JDA jda;
    private JavaPlugin plugin;
    private ConfigFile discordFile;
    private final HikariConfig hikariConfig;

    public DiscordConnector(CelestialSyncPlugin plugin, ConfigFile discordFile, HikariConfig hikariConfig) {
        this.plugin = plugin;
        this.discordFile =  new ConfigFile(this.plugin, "discord.yml");
        this.hikariConfig = hikariConfig;

    }
    @Override
    public String getToken() {
        return discordFile.getString("DISCORD.TOKEN");
    }

    @Override
    public TextChannel getTextChannel() {
        return this.jda.getTextChannelById(discordFile.getString("DISCORD.CHANNEL"));
    }

    @Override
    public Guild getGuild() {
        return this.jda.getGuildById(discordFile.getString("DISCORD.GUILD"));
    }
    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    @SneakyThrows
    public void connect() {
        this.jda = JDABuilder.createDefault(getToken())
                .addEventListeners(new SyncCommand(discordFile, hikariConfig))
                .addEventListeners(new UnSyncCommand(discordFile, hikariConfig))
                .build()
                .awaitReady();
        if (discordFile.getBoolean("MODULES.DISCORD-STATUS")) {
            setStatus();
        }
        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("sync", "Generate a 16 digit code.")
                        .setGuildOnly(true)
        );
        commands.addCommands(
                Commands.slash("unsync", "Unsync a player")
                        .addOptions(new OptionData(USER, "user", "The user to unsync")
                                .setRequired(true))
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
        );

        commands.queue();
    }

    @Override
    public void disconnect() {
        if (this.jda != null) {
            this.jda.shutdownNow();
        }
    }

    @Override
    public void setStatus() {
        String type = discordFile.getString("DISCORD.STATUS.TYPE");
        String message = discordFile.getString("DISCORD.STATUS.MESSAGE");
        this.jda.getPresence().setActivity(Activity.of(Activity.ActivityType.valueOf(type), message));
    }
}
