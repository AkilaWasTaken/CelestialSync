package dev.celestial.sync.discord.commands;

import com.zaxxer.hikari.HikariConfig;
import dev.celestial.sync.managers.DatabaseManager;
import dev.celestial.sync.utils.CC;
import dev.celestial.sync.utils.file.ConfigFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class SyncCommand extends ListenerAdapter {
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 16;
    private final Random random = new SecureRandom();
    private ConfigFile discordFile;
    private final HikariConfig hikariConfig;
    private final DataSource dataSource;
    public SyncCommand(ConfigFile discordFile, HikariConfig hikariConfig) {
        this.discordFile = discordFile;
        this.hikariConfig = hikariConfig;
        this.dataSource = DatabaseManager.getDataSource();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String targetChannelId = discordFile.getString("DISCORD.CHANNEL");
        String discordId = event.getUser().getId();
        User user = event.getUser();
        String avatarUrl = user.getAvatarUrl();
        if (!event.getChannel().getId().equals(targetChannelId)) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.WRONG_CHANNEL")))
                    .setTitle(discordFile.getString("EMBED_TITLES.WRONG_CHANNEL"))
                    .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.WRONG_CHANNEL")
                            .replace("%channel%",  "<#" + targetChannelId + ">"))
                    .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_SYNC_COMMAND")
                            .replace("%logo%", avatarUrl)
                            .replace("%user%", user.getAsTag())
                            .replace("%time%", java.time.LocalDateTime.now().toString()));
            if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                embed.setThumbnail(avatarUrl);
            }
            event.replyEmbeds(embed.build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (event.getGuild() == null) {
            return;
        }
        switch (event.getName()) {
            case "sync":
                try {
                    String fetchVerifiedQuery = "SELECT * FROM verified WHERE discord = ?";
                    try (Connection connection = this.dataSource.getConnection()) {
                        try (PreparedStatement fetchVerifiedStatement = connection.prepareStatement(fetchVerifiedQuery)) {
                            fetchVerifiedStatement.setString(1, discordId);
                            try (ResultSet verifiedResult = fetchVerifiedStatement.executeQuery()) {
                                if (verifiedResult.next()) {
                                    EmbedBuilder verifiedEmbed = new EmbedBuilder()
                                            .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.ALREADY_VERIFIED")))
                                            .setTitle(discordFile.getString("EMBED_TITLES.ALREADY_VERIFIED"))
                                            .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.ALREADY_VERIFIED"))
                                            .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_SYNC_COMMAND")
                                                    .replace("%logo%", avatarUrl)
                                                    .replace("%user%", user.getAsTag())
                                                    .replace("%time%", java.time.LocalDateTime.now().toString()));
                                    if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                                        verifiedEmbed.setThumbnail(avatarUrl);
                                    }
                                    event.replyEmbeds(verifiedEmbed.build())
                                            .setEphemeral(true)
                                            .queue();
                                    return;
                                }
                            }
                        }
                    }
                    String selectQuery = "SELECT * FROM unverified WHERE discord = ?";
                    try (Connection connection = this.dataSource.getConnection()) {
                        try (PreparedStatement selectStatement = connection.prepareStatement(selectQuery)) {
                            selectStatement.setObject(1, discordId);
                            try (ResultSet resultSet = selectStatement.executeQuery()) {
                                if (resultSet.next()) {
                                    String existingCode = resultSet.getString("code");
                                    EmbedBuilder codeEmbed = new EmbedBuilder()
                                            .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.EXISTING_CODE")))
                                            .setTitle(discordFile.getString("EMBED_TITLES.EXISTING_CODE"))
                                            .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.EXISTING_CODE")
                                                    .replace("%existingCode%", existingCode))
                                            .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_SYNC_COMMAND")
                                                    .replace("%logo%", avatarUrl)
                                                    .replace("%user%", user.getAsTag())
                                                    .replace("%time%", java.time.LocalDateTime.now().toString()));
                                    if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                                        codeEmbed.setThumbnail(avatarUrl);
                                    }
                                    event.replyEmbeds(codeEmbed.build())
                                            .setEphemeral(true)
                                            .queue();
                                    return;
                                }
                            }
                        }
                    }
                    String code = generateRandomCode();
                    String insertQuery = "INSERT INTO unverified (discord, code) VALUES (?, ?)";
                    try (Connection connection = this.dataSource.getConnection()) {
                        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
                            preparedStatement.setObject(1, discordId);
                            preparedStatement.setObject(2, code);
                            preparedStatement.executeUpdate();
                        }
                    }

                    EmbedBuilder embed = new EmbedBuilder()
                            .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.NEW_CODE")))
                            .setTitle(discordFile.getString("EMBED_TITLES.NEW_CODE"))
                            .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.NEW_CODE")
                                    .replace("%code%", code))
                            .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_SYNC_COMMAND")
                                    .replace("%logo%", avatarUrl)
                                    .replace("%user%", user.getAsTag())
                                    .replace("%time%", java.time.LocalDateTime.now().toString()));
                    if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                        embed.setThumbnail(avatarUrl);
                    }

                    event.replyEmbeds(embed.build())
                            .setEphemeral(true)
                            .queue();
                } catch (Exception e) {
                    event.deferReply(true).setContent(discordFile.getString("ERROR_MESSAGES.GENERATE_CODE_ERROR")).queue();
                    e.printStackTrace();
                }
                break;
        }
    }



    private String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }
}
