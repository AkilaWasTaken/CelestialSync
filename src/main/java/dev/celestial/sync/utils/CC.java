package dev.celestial.sync.utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.awt.*;

public class CC {
    public static String colour (String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String out (String out) {
        Bukkit.getConsoleSender().sendMessage(colour("[CelestialSync] " + out));
        return out;
    }
    public static void sendPluginStartupMessage() {
        //Calvin S
        out("╔═╗┌─┐┬  ┌─┐┌─┐┌┬┐┬┌─┐┬  ╔═╗┬ ┬┌┐┌┌─┐");
        out("║  ├┤ │  ├┤ └─┐ │ │├─┤│  ╚═╗└┬┘││││  ");
        out("╚═╝└─┘┴─┘└─┘└─┘ ┴ ┴┴ ┴┴─┘╚═╝ ┴ ┘└┘└─┘");
        out("");
    }
    public static Color getColorFromHex(String hexColor) {
        try {
            return Color.decode(hexColor);
        } catch (NumberFormatException e) {
            return Color.WHITE;
        }
    }



}

