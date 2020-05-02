package me.uniodex.skywars.managers;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.objects.CustomScoreboard;

public class ScoreboardManager {

    private Skywars plugin;

    public ScoreboardManager(Skywars plugin) {
        this.plugin = plugin;
    }

    public CustomScoreboard createScoreboard(String[] lines) {
        return this.createScoreboard(plugin.customization.scoreboard_title, lines);
    }

    public CustomScoreboard createScoreboard(String title, String[] lines) {
        return new CustomScoreboard(plugin, true, title, lines);
    }
}
