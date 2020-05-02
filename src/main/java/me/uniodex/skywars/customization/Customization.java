package me.uniodex.skywars.customization;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Recode and make safe after 4.0
public class Customization {

    public String prefix;

    public HashMap<String, String> messages, inventories, lores, deathMessages, titles;
    public List<String> killMessages;
    public String player_suicide;

    public HashMap<String, String> scoreboard;
    public String scoreboard_title;
    String scoreboard_header, scoreboard_footer;

    public Customization(FileConfiguration file) {
        prefix = c(file.getString("prefix"));

        messages = new HashMap<String, String>();
        for (String key : file.getConfigurationSection("Messages").getKeys(false))
            messages.put(key, prefix + c(file.getString("Messages." + key)));

        inventories = new HashMap<String, String>();
        for (String key : file.getConfigurationSection("Inventories").getKeys(false))
            inventories.put(key, c(file.getString("Inventories." + key)));

        lores = new HashMap<String, String>();
        for (String key : file.getConfigurationSection("Lores").getKeys(false))
            lores.put(key, c(file.getString("Lores." + key)));

        titles = new HashMap<String, String>();
        for (String key : file.getConfigurationSection("TitleManager").getKeys(false))
            titles.put(key, c(file.getString("TitleManager." + key)));

        deathMessages = new HashMap<String, String>();
        for (String cause : file.getConfigurationSection("Death-Messages").getKeys(false)) {
            if (!cause.equalsIgnoreCase("PLAYER"))
                deathMessages.put(cause.toUpperCase(), prefix + c(file.getString("Death-Messages." + cause)));
        }

        killMessages = new ArrayList<String>();
        for (String message : file.getStringList("Death-Messages.PLAYER.OTHER")) {
            killMessages.add(prefix + c(message));
        }
        player_suicide = prefix + c(file.getString("Death-Messages.PLAYER.SUICIDE"));

        scoreboard = new HashMap<String, String>();
        for (String key : file.getConfigurationSection("Scoreboard.Tags").getKeys(false)) {
            scoreboard.put(key, c(file.getString("Scoreboard.Tags." + key)));
        }
        scoreboard_title = c(file.getString("Scoreboard.Title"));
        scoreboard_header = c(file.getString("Scoreboard.Header"));
        scoreboard_footer = c(file.getString("Scoreboard.Footer"));
    }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
