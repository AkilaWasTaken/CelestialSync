package dev.celestial.sync.model;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public interface IDiscordModel {

    String getToken();
    TextChannel getTextChannel();

    Guild getGuild();
    JDA getJDA();
    void connect();
    void disconnect();
    void setStatus();
}