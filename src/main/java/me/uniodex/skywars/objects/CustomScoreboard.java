package me.uniodex.skywars.objects;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.enums.SpecialCharacter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Set;

public class CustomScoreboard {

    private Scoreboard scoreboard;
    private Objective objective;
    private HashMap<Integer, String> scores = new HashMap<Integer, String>();
    private boolean showHealth;

    public CustomScoreboard(Skywars plugin, boolean showHealth, String name, String... lines) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("SW", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(format(name));
        for (int i = 0; i < lines.length; i++) {
            while (scoreboard.getEntries().contains(lines[i])) lines[i] += " ";
            String scoreName = format(lines[i]);
            int score = lines.length - i;
            objective.getScore(scoreName).setScore(score);
            scores.put(score, scoreName);
        }

        this.showHealth = showHealth;
        if (showHealth) {
            //This displays health under players name.
            Objective healthObjective = scoreboard.registerNewObjective("SW_HEALTH", "health");
            healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
            healthObjective.setDisplayName(ChatColor.DARK_RED + SpecialCharacter.HEART.toString());
        }

    }

    public void update(String entryName, String string, boolean below, int scoregiven) {
        for (String entry : scoreboard.getEntries()) {
            if (entry != null) {
                if (entry.contains(entryName)) {
                    int score = objective.getScore(entry).getScore();
                    if (below) score--;
                    if (scoregiven == 6) {
                        score = 6;
                    }
                    if (scores.get(score) == null) {
                        break;
                    }
                    scoreboard.resetScores(scores.get(score));
                    while (scoreboard.getEntries().contains(string)) string += " ";
                    String scoreString = format(string);
                    objective.getScore(scoreString).setScore(score);
                    scores.put(score, scoreString);
                    break;
                }
            }
        }
    }

    public void update(String entryName, int value, boolean below, int scoregiven) {
        update(entryName, String.valueOf(value), below, scoregiven);
    }

    public void updateEntire(String name, String... lines) {
        objective.unregister();
        scores.clear();
        objective = scoreboard.registerNewObjective("SW", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(format(name));

        for (int i = 0; i < lines.length; i++) {
            while (scoreboard.getEntries().contains(lines[i])) lines[i] += " ";
            String scoreName = format(lines[i]);
            int score = lines.length - i;
            objective.getScore(scoreName).setScore(score);
            scores.put(score, scoreName);
        }
    }

    private String format(String text) {
        return text.length() > 16 ? Bukkit.getBukkitVersion().contains("1.7") ? text.substring(0, 16) : text.length() > 32 ? text.substring(0, 32) : text : text;
    }

    public Team registerTeam(String name) {
        return scoreboard.registerNewTeam(name);
    }

    public Set<Team> getTeams() {
        return scoreboard.getTeams();
    }

    public void setName(String name) {
        objective.setDisplayName(name);
    }

    public void apply(Player p) {
        p.setScoreboard(scoreboard);
        if (showHealth) p.setHealth(p.getHealth() - 0.0001);
    }

}
