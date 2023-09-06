package dev.celestial.sync.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import com.zaxxer.hikari.HikariConfig;
import dev.celestial.sync.managers.DatabaseManager;
import dev.celestial.sync.utils.CC;
import dev.celestial.sync.utils.file.ConfigFile;
import dev.celestial.sync.CelestialSyncPlugin;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@CommandAlias("sync")
public class CmdSync extends BaseCommand {
    private ConfigFile minecraftFile;
    private final HikariConfig hikariConfig;
    private final DataSource dataSource;
    private CelestialSyncPlugin plugin;

    public CmdSync(CelestialSyncPlugin plugin, ConfigFile minecraftFile, HikariConfig hikariConfig) {
        this.plugin = plugin;
        this.minecraftFile = new ConfigFile(plugin, "minecraft.yml");
        this.hikariConfig = hikariConfig;
        this.dataSource = DatabaseManager.getDataSource();
    }

    @Default
    public void Sync(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.SYNTAX")));
        }
            String codeToVerify = args[0];
            String verifyQuery = "SELECT * FROM unverified WHERE code = ?";
            String insertVerifiedQuery = "INSERT INTO verified (uuid, discord, code) VALUES (?, ?, ?)";
            String deleteUnverifiedQuery = "DELETE FROM unverified WHERE code = ?";
            String fetchVerifiedQuery = "SELECT * FROM verified WHERE uuid = ?";
            UUID senderUUID = player.getUniqueId();

            try (Connection connection = this.dataSource.getConnection()) {
                try (PreparedStatement fetchVerifiedStatement = connection.prepareStatement(fetchVerifiedQuery)) {
                    fetchVerifiedStatement.setString(1, senderUUID.toString());
                    try (ResultSet verifiedResult = fetchVerifiedStatement.executeQuery()) {
                        if (verifiedResult.next()) {
                            player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.ALREADY_VERIFIED")));
                            return;
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            try (Connection connection = this.dataSource.getConnection()) {
                try (PreparedStatement verifyStatement = connection.prepareStatement(verifyQuery)) {
                    verifyStatement.setString(1, codeToVerify);
                    try (ResultSet resultSet = verifyStatement.executeQuery()) {
                        if (resultSet.next()) {
                            String verifiedCode = resultSet.getString("code");
                            String discordId = resultSet.getString("discord");

                            if (discordId != null && !discordId.isEmpty()) {

                                User discordUser = plugin.getDiscord().getJDA().retrieveUserById(discordId).complete();
                                if (discordUser != null) {
                                    Guild guild = plugin.getDiscord().getGuild();
                                    Member member = guild.retrieveMember(discordUser).complete();

                                    if (member != null) {
                                        String newNickname = player.getName();
                                        guild.modifyNickname(member, newNickname).queue(
                                                success -> player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.NICKNAME_UPDATED")))
                                        );
                                        if (minecraftFile.getBoolean("MODULES.DISCORD_ROLE.ENABLED")) {
                                            String roleId = minecraftFile.getString("MODULES.DISCORD_ROLE.ROLE_ID");
                                            if (roleId != null && !roleId.isEmpty()) {
                                                Role role = guild.getRoleById(roleId);
                                                if (role != null) {
                                                    guild.addRoleToMember(member, role).queue(
                                                            roleAdded -> player.sendMessage(CC.colour(minecraftFile.getString("MODULES.DISCORD_ROLE.NICKNAME_UPDATED_AND_ROLE_GRANTED"))),
                                                            error -> player.sendMessage(CC.colour(minecraftFile.getString("MODULES.DISCORD_ROLE.NICKNAME_UPDATED_ROLE_ERROR")))
                                                    );
                                                } else {
                                                    player.sendMessage(CC.colour(minecraftFile.getString("MODULES.DISCORD_ROLE.ROLE_NOT_FOUND")));
                                                }
                                            } else {
                                                player.sendMessage(CC.colour(minecraftFile.getString("MODULES.DISCORD_ROLE.ROLE_ID_NOT_CONFIGURED")));
                                            }
                                        }
                                    }
                                }
                            }
                            try (PreparedStatement insertVerifiedStatement = connection.prepareStatement(insertVerifiedQuery)) {
                                insertVerifiedStatement.setString(1, senderUUID.toString());
                                insertVerifiedStatement.setString(2, discordId);
                                insertVerifiedStatement.setString(3, verifiedCode);
                                insertVerifiedStatement.executeUpdate();
                            }

                            try (PreparedStatement deleteUnverifiedStatement = connection.prepareStatement(deleteUnverifiedQuery)) {
                                deleteUnverifiedStatement.setString(1, verifiedCode);
                                deleteUnverifiedStatement.executeUpdate();
                            }

                            player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.VERIFIED_AND_UPDATED")));
                            if (minecraftFile.getBoolean("MODULES.REWARD.ENABLED")) {
                                List<String> consoleCommands = minecraftFile.getStringList("MODULES.REWARD.COMMANDS");

                                if (!consoleCommands.isEmpty()) {
                                    for (String command : consoleCommands) {
                                        String formattedCommand = command.replace("%player%", player.getUniqueId().toString());
                                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), formattedCommand);
                                    }
                                }
                            }


                            if (minecraftFile.getBoolean("MODULES.ANNOUNCER.ENABLED")) {
                                List<String> announcementLines = minecraftFile.getStringList("MODULES.ANNOUNCER.ANNOUNCEMENT");

                                if (!announcementLines.isEmpty()) {
                                    for (String line : announcementLines) {
                                        String formattedLine = line.replace("%player%", player.getName());
                                        Bukkit.broadcastMessage(CC.colour(formattedLine));
                                    }
                                }
                            }
                        } else {
                            player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.CODE_NOT_FOUND")));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(CC.colour(minecraftFile.getString("MESSAGES.ERROR_WHILE_VERIFYING")));
            }
        }
}
