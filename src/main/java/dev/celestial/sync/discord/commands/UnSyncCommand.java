package dev.celestial.sync.discord.commands;

import com.zaxxer.hikari.HikariConfig;
import dev.celestial.sync.managers.DatabaseManager;
import dev.celestial.sync.utils.CC;
import dev.celestial.sync.utils.file.ConfigFile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UnSyncCommand extends ListenerAdapter {
    private ConfigFile discordFile;
    private final HikariConfig hikariConfig;
    private final DataSource dataSource;

    public UnSyncCommand(ConfigFile discordFile, HikariConfig hikariConfig) {
        this.discordFile = discordFile;
        this.hikariConfig = hikariConfig;
        this.dataSource = DatabaseManager.getDataSource();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        User user = event.getUser();

        if (event.getGuild() == null) {
            return;
        }

        switch (event.getName()) {
            case "unsync":
                Member member = event.getOption("user").getAsMember();
                if (member != null) {
                    if (!event.getMember().hasPermission(Permission.BAN_MEMBERS))
                    {
                        EmbedBuilder NoPermission = new EmbedBuilder()
                                .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.NO_PERMISSION")))
                                .setTitle(discordFile.getString("EMBED_TITLES.NO_PERMISSION"))
                                .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.NO_PERMISSION")
                                        .replace("%user%", "<@" + user.getAsTag() + ">"))
                                .setFooter(discordFile.getString("EMBED_FOOTERS.NO_PERMISSION")
                                        .replace("%user%", user.getAsTag())
                                        .replace("%time%", java.time.LocalDateTime.now().toString()));
                        event.replyEmbeds(NoPermission.build())
                                .setEphemeral(true)
                                .queue();
                        return;
                    }


                    String targetUser = member.getId();
                    User avatar = member.getUser();
                    String avatarUrl = avatar.getAvatarUrl();
                    if (targetUser != null) {
                        try {
                            String deleteQuery = "DELETE FROM verified WHERE discord = ?";
                            try (Connection connection = this.dataSource.getConnection()) {
                                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                                    deleteStatement.setObject(1, targetUser);
                                    int rowsDeleted = deleteStatement.executeUpdate();
                                    if (rowsDeleted > 0) {
                                        EmbedBuilder successEmbed = new EmbedBuilder()
                                                .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.UNSYNC_SUCCESSFUL")))
                                                .setTitle(discordFile.getString("EMBED_TITLES.UNSYNC_SUCCESSFUL"))
                                                .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.UNSYNC_SUCCESSFUL")
                                                        .replace("%user%", "<@" + user.getAsTag() + ">"))
                                                .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_UNSYNC_COMMAND")
                                                        .replace("%user%", user.getAsTag())
                                                        .replace("%time%", java.time.LocalDateTime.now().toString()));
                                        if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                                            successEmbed.setThumbnail(avatarUrl);
                                        }
                                        event.replyEmbeds(successEmbed.build())
                                                .setEphemeral(true)
                                                .queue();
                                    } else {
                                        EmbedBuilder notFoundEmbed = new EmbedBuilder()
                                                .setColor(CC.getColorFromHex(discordFile.getString("EMBED_COLORS.UNSYNC_ERROR")))
                                                .setTitle(discordFile.getString("EMBED_TITLES.UNSYNC_ERROR"))
                                                .setDescription(discordFile.getString("EMBED_DESCRIPTIONS.UNSYNC_ERROR")
                                                        .replace("%user%", user.getAsTag()))
                                                .setFooter(discordFile.getString("EMBED_FOOTERS.GENERATED_BY_UNSYNC_COMMAND")
                                                        .replace("%user%", user.getAsTag())
                                                        .replace("%time%", java.time.LocalDateTime.now().toString()));
                                        if (discordFile.getBoolean("MODULES.AVATAR_THUMBNAIL")) {
                                            notFoundEmbed.setThumbnail(avatarUrl);
                                        }
                                        event.replyEmbeds(notFoundEmbed.build())
                                                .setEphemeral(true)
                                                .queue();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            event.deferReply(true).setContent(discordFile.getString("ERROR_MESSAGES.UNSYNC_ERROR")).queue();
                            e.printStackTrace();
                        }
                    }
                    break;
                }
        }
    }
}

