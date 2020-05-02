package me.uniodex.skywars.customization;

import me.uniodex.skywars.Skywars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Recode and make safe after 4.0
public class Config {

    public boolean emptyChunkGenerator;
    public boolean useUUID;
    public boolean mysql_enabled;

    public int startingCoins;

    public int coinsPerKill;
    public int coinsPerWin;

    public int celebrationLength;
    public int maxWarnings;
    public int maxArenaSize;
    public int leaveCountdownSeconds;

    public boolean lobbyChat, inGameChat, spectatorsChat;
    public boolean lobbyScoreboardEnabled;
    public boolean allowSpectatorJoin;

    public boolean allowSpectatorsViewInventory;
    public boolean loadSkinsOnSkulls;
    public boolean disableNaturalMobSpawning;
    public boolean fireworksEnabled;
    public boolean leashMobs, teleportMobs;
    public boolean voidInstantKill;
    public boolean showHealthOnBowHit;

    public List<Integer> broadcastTime;
    public List<String> allowedCommands;

    public int chestChecks_slotOverwrite;
    public int chestChecks_itemDuplicate;

    public List<String> executed_commands_player_win;
    public List<String> executed_commands_arena_start;
    public List<String> executed_commands_arena_countdown;

    public String bungee_mode_hub;

    public int rollback_scan_speed;
    public int rollback_repair_speed;
    public int rollback_queue_size;
    public int rollback_send_status_update_every;

    public ChatColor defaultTeamColor;
    public boolean teamColorRandomizerEnabled;
    public ChatColor[] teamColorRandomizerColors;

    public boolean scoreboardTitleAnimationEnabled;
    public int scoreboardTitleAnimationInterval;
    public List<String> scoreboardTitleAnimationFrames;

    public boolean titles_enabled, actionbar_enabled;
    public int titles_fadeIn, titles_stay, titles_fadeOut;

    public HashMap<String, Integer> hotbarItems;
    public String gameMode;

    public Config(Skywars plugin) {
        FileConfiguration file = plugin.getConfig();
        gameMode = file.getString("gameMode");
        emptyChunkGenerator = file.getBoolean("Empty-Chunk-Generator");
        useUUID = file.getBoolean("use-UUID");
        mysql_enabled = file.getBoolean("MySQL.enabled");

        startingCoins = file.getInt("String-Coins");

        coinsPerKill = file.getInt("Coins-Per-Kill");
        coinsPerWin = file.getInt("Coins-Per-Win");

        celebrationLength = file.getInt("Celebration-Length");
        maxWarnings = file.getInt("Max-Warnings");
        maxArenaSize = file.getInt("Max-Arena-Size");
        leaveCountdownSeconds = file.getInt("Leave-Countdown-Seconds");

        lobbyChat = file.getBoolean("Lobby-Chat");
        inGameChat = file.getBoolean("In-Game-Chat");
        spectatorsChat = file.getBoolean("Spectators-Chat");
        lobbyScoreboardEnabled = file.getBoolean("Lobby-Scoreboard-Enabled");
        allowSpectatorJoin = file.getBoolean("Allow-Spectator-Join");

        allowSpectatorsViewInventory = file.getBoolean("Allow-Spectators-Inventory-View");
        loadSkinsOnSkulls = file.getBoolean("Load-Skins-On-Skulls");
        disableNaturalMobSpawning = file.getBoolean("Disable-Natural-Mob-Spawning");
        fireworksEnabled = file.getBoolean("Fireworks-Enabled");
        leashMobs = file.getBoolean("Leash-Mobs");
        teleportMobs = file.getBoolean("Teleport-Mobs");
        voidInstantKill = file.getBoolean("Void-Instant-Kill");
        showHealthOnBowHit = file.getBoolean("Show-Health-On-Bow-Hit");

        broadcastTime = file.getIntegerList("Broadcast-Time");
        allowedCommands = new ArrayList<String>();
        for (String command : file.getStringList("Allowed-Commands")) allowedCommands.add(command.toLowerCase());

        chestChecks_slotOverwrite = Math.min(Math.max(1, file.getInt("Chest-Checks.Slot-Overwrite")), 10);
        chestChecks_itemDuplicate = Math.min(Math.max(1, file.getInt("Chest-Checks.Item-Duplicate")), 10);

        executed_commands_player_win = file.getStringList("Executed-Commands.Player-Win");
        executed_commands_arena_start = file.getStringList("Executed-Commands.Arena-Start");
        executed_commands_arena_countdown = file.getStringList("Executed-Commands.Arena-Countdown");

        bungee_mode_hub = file.getString("Bungee-Mode.hub");

        rollback_scan_speed = file.getInt("Rollback.Scan-Speed");
        rollback_repair_speed = file.getInt("Rollback.Repair-Speed");
        rollback_queue_size = file.getInt("Rollback.Queue-Size");
        rollback_send_status_update_every = file.getInt("Rollback.Send-Status-Update-Every");

        defaultTeamColor = ChatColor.getByChar(file.getString("Team-Coloring.default").replace("&", ""));
        teamColorRandomizerEnabled = file.getBoolean("Team-Coloring.randomizer-enabled");
        if (teamColorRandomizerEnabled) {
            teamColorRandomizerColors = new ChatColor[file.getStringList("Team-Coloring.randomizer-colors").size()];
            int i = 0;
            for (String colorCode : file.getStringList("Team-Coloring.randomizer-colors")) {
                teamColorRandomizerColors[i] = ChatColor.getByChar(colorCode.replace("&", ""));
                i++;
            }
        } else teamColorRandomizerColors = new ChatColor[0];

        scoreboardTitleAnimationEnabled = file.getBoolean("Scoreboard-Title-Animation.enabled");
        scoreboardTitleAnimationInterval = file.getInt("Scoreboard-Title-Animation.interval");
        scoreboardTitleAnimationFrames = new ArrayList<String>();
        for (String frame : file.getStringList("Scoreboard-Title-Animation.frames")) {
            scoreboardTitleAnimationFrames.add(ChatColor.translateAlternateColorCodes('&', frame));
        }
        if (scoreboardTitleAnimationFrames.size() < 2) {
            scoreboardTitleAnimationEnabled = false;
            Bukkit.getConsoleSender().sendMessage(plugin.customization.prefix + "Lobby scoreboard animation was disabled because there are not enough amount of frames!");
        } else if (scoreboardTitleAnimationEnabled)
            Bukkit.getConsoleSender().sendMessage(plugin.customization.prefix + "Scoreboard title animation has been enabled and it contains " + ChatColor.AQUA + scoreboardTitleAnimationFrames.size() + ChatColor.GRAY + " frame(s)!");


        boolean titleManager = Bukkit.getPluginManager().getPlugin("TitleManager") != null && Bukkit.getPluginManager().getPlugin("TitleManager").isEnabled();
        if (titleManager) {
            Bukkit.getConsoleSender().sendMessage(plugin.customization.prefix + "TitleManager has been detected! TitleManager features are now accessible..");
            titles_enabled = file.getBoolean("Titles.enabled");
            titles_fadeIn = 10;
            titles_stay = 40;
            titles_fadeOut = 10;
            actionbar_enabled = file.getBoolean("Action-Bar-Enabled");
        } else {
            titles_enabled = false;
            actionbar_enabled = false;
        }

        hotbarItems = new HashMap<String, Integer>();
        for (String item : file.getConfigurationSection("Hotbar-Items").getKeys(false)) {
            if (file.getBoolean("Hotbar-Items." + item + ".enabled"))
                hotbarItems.put(item, file.getInt("Hotbar-Items." + item + ".slot"));
        }
    }

}
