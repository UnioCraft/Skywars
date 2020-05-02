package me.uniodex.skywars.arena;

import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.objects.Cage;
import me.uniodex.skywars.objects.CustomScoreboard;
import me.uniodex.skywars.objects.Kit;
import me.uniodex.skywars.player.SWOfflinePlayer;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.ItemStackBuilder;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.jsonmessage.JSONMessage;
import me.uniodex.skywars.utils.packages.titleapi.TitleAPI;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class Arena {

    private Skywars plugin;
    private VotesManager votesManager;

    // Arena Details
    private String arenaName;
    private boolean enabled;
    private int teamSize;
    private int minTeams;
    private int maxTeams;
    private String gameMode; // SOLO, DUO
    private int lobbyCountdown;
    private int gameLength;
    private List<Integer> refillTimes;
    private Cuboid cuboid;
    private ArenaState arenaState;
    private Location spectatorsLocation;
    private String currentEvent;
    private String nextEvent;

    private HashMap<Team, TeamData> teams;
    private HashMap<SWOnlinePlayer, Team> players;
    private HashMap<Location, String> chests;
    private HashMap<Location, Integer> chestSpawnPoints;
    private HashMap<String, Integer> killers;
    private ArrayList<SWOnlinePlayer> spectators;
    private CustomScoreboard scoreboard;
    private int oyunKodu = 0;
    //0 - Lobby task, 1 - Game task, 2 - Ending task
    private BukkitTask[] tasks;

    public Arena(Skywars plugin, String arenaName) {
        this.plugin = plugin;
        this.arenaName = arenaName;
        init();
    }

    private void init() {
        FileConfiguration arenaConfig = plugin.configManager.getArenaConfig(arenaName);
        FileConfiguration locationsConfig = plugin.configManager.getArenaLocations(arenaName);
        votesManager = new VotesManager(plugin);

        enabled = arenaConfig.getBoolean("enabled");

        teamSize = arenaConfig.getInt("team-size");
        if (teamSize == 1) {
            gameMode = "solo";
        } else {
            gameMode = "duo";
        }

        minTeams = arenaConfig.getInt("min-teams");
        if (minTeams < 2) {
            minTeams = 2;
        }
        maxTeams = arenaConfig.getInt("max-teams");
        lobbyCountdown = arenaConfig.getInt("lobby-countdown");
        gameLength = arenaConfig.getInt("game-length");
        refillTimes = arenaConfig.getIntegerList("refill-times");

        cuboid = new Cuboid(locationsConfig.getString("Cuboid"));
        chests = new HashMap<Location, String>();
        chestSpawnPoints = new HashMap<Location, Integer>();
        players = new HashMap<SWOnlinePlayer, Team>();
        killers = new HashMap<String, Integer>();
        tasks = new BukkitTask[3];
        spectators = new ArrayList<SWOnlinePlayer>();
        teams = new HashMap<Team, TeamData>();

        //arenaState = enabled ? ArenaState.QUEUED : ArenaState.DISABLED;
        this.setArenaState(ArenaState.ROLLBACKING);
        plugin.worldManager.rollbackWorld(cuboid.worldName, this);

    }

    public void initPart2() {
        resetScoreboard();
        resetLocations();
        resetOyunKodu();

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            public void run() {
                scoreboard.update("§8ID: #", "§8ID: #" + getOyunKodu(), false, 0);
                setArenaState(enabled ? ArenaState.WAITING : ArenaState.DISABLED);
            }
        }, 25L);
    }

    // Main Gameplay Functions
    public void joinPlayer(SWOnlinePlayer swPlayer, boolean asSpectator) {
        Player player = swPlayer.getPlayer();

        if (swPlayer.getArena() != null) {
            swPlayer.getArena().quitPlayer(swPlayer.getName());
        }

        if (asSpectator) {
            if (!arenaState.isAvailable()) {
                plugin.bukkitPlayerManager.preparePlayer(player, "spectating");
                swPlayer.setArena(this);
                spectators.add(swPlayer);
                players.put(swPlayer, null);
                scoreboard.apply(player);
                player.teleport((this.spectatorsLocation != null) ? this.spectatorsLocation : this.cuboid.getRandomLocation());
                player.sendMessage(plugin.customization.prefix + "§cOyuna izleyici olarak giriş yaptınız.");
                return;
            } else {
                player.sendMessage(plugin.customization.prefix + ChatColor.RED + "Başlamamış oyunlara izleyici olarak giremezsiniz.");
                return;
            }
        }

        if (!isJoinable()) {
            player.sendMessage(plugin.customization.messages.get("Arena-Not-Available"));
            return;
        }

        if (getCurrentTeamAmount() < getMaxTeamAmount()) {
            player.sendMessage(plugin.customization.messages.get("Arena-Not-Enough-Spawnpoints"));
            return;
        }

        Team team = null;
        if (getArenaMode().equalsIgnoreCase("duo")) {
            if (swPlayer.getSWOfflinePlayer().getParty() != null) {
                for (Team existTeam : getTeams().keySet()) {
                    TeamData teamData = getTeamData(existTeam);
                    for (PAFPlayer pafpartyPlayer : swPlayer.getSWOfflinePlayer().getParty().getAllPlayers()) {
                        SWOnlinePlayer partyPlayer = plugin.playerManager.getSWOnlinePlayer(pafpartyPlayer.getName());
                        if (partyPlayer == null) {
                            continue;
                        }

                        if (teamData.isTeamReserved()
                                && teamData.getTeamLeader().equals(partyPlayer)
                                && existTeam.getSize() + 1 <= getPlayerSizePerTeam()) {
                            team = existTeam;
                            swPlayer.setArena(this);
                        }
                    }
                }

                if (team == null) {
                    int remainingPlayer = 0;
                    for (PAFPlayer pafpartyPlayer : swPlayer.getSWOfflinePlayer().getParty().getAllPlayers()) {
                        SWOnlinePlayer partyPlayer = plugin.playerManager.getSWOnlinePlayer(pafpartyPlayer.getName());
                        if (partyPlayer == null) {
                            continue;
                        }
                        if (partyPlayer.getArena() == null) {
                            remainingPlayer++;
                        }
                    }
                    if (remainingPlayer > 0) {
                        team = getAvailableTeam(2);
                        if (team == null) {
                            player.sendMessage(plugin.customization.messages.get("Arena-Full"));
                            return;
                        }
                        swPlayer.setArena(this);
                        TeamData teamData = getTeams().get(team);
                        teamData.setTeamReserved(true);
                        teamData.setTeamLeader(swPlayer);
                    } else {
                        team = getAvailableTeam(1);
                        if (team == null) {
                            player.sendMessage(plugin.customization.messages.get("Arena-Full"));
                            return;
                        }
                        swPlayer.setArena(this);
                    }
                }
            } else {
                team = getAvailableTeam(1);
                if (team == null) {
                    player.sendMessage(plugin.customization.messages.get("Arena-Full"));
                    return;
                }
                swPlayer.setArena(this);
            }
        } else {
            team = getAvailableTeam(1);
            if (team == null) {
                player.sendMessage(plugin.customization.messages.get("Arena-Full"));
                return;
            }
            swPlayer.setArena(this);
        }

        TeamData teamData = getTeamData(team);

        plugin.bukkitPlayerManager.preparePlayer(player, "join");

        if (getArenaMode().equalsIgnoreCase("solo")) {
            if (team.getSize() == 0) {
                Cage cage;
                if (plugin.buyableItemManager.getCages().containsKey(swPlayer.getSelectedItem("cage"))) {
                    cage = plugin.buyableItemManager.getCages().get(swPlayer.getSelectedItem("cage"));
                } else {
                    cage = plugin.buyableItemManager.getCages().get("Default");
                }
                buildCage(cage, teamData.getSmallCage());
            }
        } else {
            if (team.getSize() == 0) {
                Cage cage;
                if (plugin.buyableItemManager.getCages().containsKey(swPlayer.getSelectedItem("cage"))) {
                    cage = plugin.buyableItemManager.getCages().get(swPlayer.getSelectedItem("cage"));
                } else {
                    cage = plugin.buyableItemManager.getCages().get("Default");
                }
                buildCage(cage, teamData.getLargeCage());
            }
        }

        team.addPlayer(Bukkit.getOfflinePlayer(swPlayer.getName()));
        scoreboard.apply(player);
        this.players.put(swPlayer, team);
        player.teleport(teamData.getSpawnpoint());

        final List<Player> arenaPlayers = this.getPlayers();
        String displayName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(player.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(player.getName()) : player.getName();
        final String playerJoinedMessage = this.plugin.customization.messages.get("Player-Join-Arena").replace("%player%", displayName).replace("%players%", String.valueOf(this.players.size())).replace("%maxplayers%", String.valueOf(this.teamSize * this.maxTeams));
        for (Player p : arenaPlayers) {
            p.sendMessage(playerJoinedMessage);
            p.showPlayer(player);
            player.showPlayer(p);
        }

        final String playerJoinedTeamMessage = this.plugin.customization.messages.get("Player-Join-Team").replace("%player%", displayName).replace("%teamsize%", String.valueOf(team.getSize())).replace("%maxteamsize%", String.valueOf(this.teamSize));
        for (final OfflinePlayer offlinePlayer : team.getPlayers()) {
            if (player instanceof Player && !offlinePlayer.getName().equals(player.getName())) {
                ((Player) offlinePlayer).sendMessage(playerJoinedTeamMessage);
            }
        }

        if (getArenaMode().equals("solo")) {
            if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(player.getName())) {
                player.setPlayerListName("§7" + displayName);
                //SkinChanger.nick(player, new Nickname(UUIDFetcher.getUUID(displayName), ChatColor.GRAY, displayName));
            } else {
                player.setPlayerListName(player.hasPermission("rank.vip") ? "§b" + displayName : "§7" + displayName);
                //SkinChanger.nick(player, new Nickname(UUIDFetcher.getUUID(displayName), player.hasPermission("rank.vip") ? "§b" + displayName :  "§7" + displayName));
            }
        } else {
            if (plugin.bukkitPlayerManager.disguisedPlayers.containsKey(player.getName())) {
                player.setPlayerListName(team.getPrefix() + displayName);
                //SkinChanger.nick(player, new Nickname(UUIDFetcher.getUUID(displayName), team.getPrefix() + displayName));
            } else {
                player.setPlayerListName(team.getPrefix() + displayName);
            }
        }

        scoreboard.update("§fOyuncular: ", "§fOyuncular: " + ChatColor.GREEN + this.players.size() + "/" + teamSize * maxTeams, false, 0);
        scoreboard.update("Oyuncular", " ", false, 6); // Interesting bug fix.

        if (this.getAliveTeams().size() >= this.minTeams && this.tasks[0] == null) {
            this.countdown();
        }
    }

    private void countdown() {
        setArenaState(ArenaState.STARTING);
        for (String command : plugin.config.executed_commands_arena_countdown) {
            String commandToExecuted = command.replace("%arena%", getArenaName()).replace("%seconds%", String.valueOf(lobbyCountdown));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecuted);
        }

        tasks[0] = new BukkitRunnable() {
            int seconds = lobbyCountdown;

            public void run() {

                if (plugin.config.broadcastTime.contains(seconds)) {
                    String message = plugin.customization.messages.get("Arena-Starting-Countdown").replace("%seconds%", String.valueOf(seconds));
                    for (Player p : getPlayers()) {
                        p.sendMessage(message);
                        p.playSound(p.getLocation(), plugin.CLICK, 1, 1);
                        if (seconds == 5) {
                            TitleAPI.sendTitle(p, 5, 20, 5, "§e⑤", "");
                        } else if (seconds == 4) {
                            TitleAPI.sendTitle(p, 5, 20, 5, "§e④", "");
                        } else if (seconds == 3) {
                            TitleAPI.sendTitle(p, 5, 20, 5, "§e➂", "");
                        } else if (seconds == 2) {
                            TitleAPI.sendTitle(p, 5, 20, 5, "§e➁", "");
                        } else if (seconds == 1) {
                            TitleAPI.sendTitle(p, 5, 20, 5, "§e➀", "");
                        }
                    }
                }

                scoreboard.update(ChatColor.WHITE + "Geri Sayım: ", ChatColor.WHITE + "Geri Sayım: " + ChatColor.GREEN + seconds + "s", false, 0);

                if (seconds == 0) {
                    cancelTask("countdown");
                    start();
                } else {
                    seconds--;
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void start() {
        setArenaState(ArenaState.INGAME);
        cancelTask("countdown");
        destroyCages();

        for (String command : plugin.config.executed_commands_arena_start) {
            String commandToExecuted = command.replace("%arena%", getArenaName()).replace("%world%", getArenaWorldName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToExecuted);
        }

        for (Player arenaPlayer : getPlayers()) {
            for (Player arenaPlayer2 : getPlayers()) {
                if (arenaPlayer != arenaPlayer2) {
                    arenaPlayer.showPlayer(arenaPlayer2);
                    arenaPlayer2.showPlayer(arenaPlayer);
                }
            }
        }

        for (Team team : getTeams().keySet()) {
            team.setAllowFriendlyFire(false);
        }

        for (Player player : this.getPlayers()) {
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());

            player.closeInventory();

            String arenaStartMessage = plugin.customization.messages.get("Arena-Start");
            player.sendMessage(arenaStartMessage);
            TitleAPI.sendTitle(player, 10, 40, 10, "§6Oyun başladı!", "§2İyi şanslar!");

            plugin.bukkitPlayerManager.preparePlayer(player, "start");

            swPlayer.addCooldown("fall", 2);

            // Give Kit
            String kitName = swPlayer.getSelectedItem("kit");
            Kit kit = plugin.buyableItemManager.getKits().get(kitName);
            plugin.bukkitPlayerManager.giveKit(player, kit);
            player.sendMessage(plugin.customization.messages.get("Kit-Receive").replace("%kit%", kit.itemName));

            // Update Stats
            plugin.playerManager.giveStat(swPlayer, "playedGames", 1);
            swPlayer.updatePlayTime();
        }

        String chestmode = votesManager.getVoted("Chests");
        if (votesManager.getVoted("Chests").equalsIgnoreCase("Insane")) {
            chestmode = "§cInsane";
        } else if (votesManager.getVoted("Chests").equalsIgnoreCase("Normal")) {
            chestmode = "§aNormal";
        } else if (votesManager.getVoted("Chests").equalsIgnoreCase("Default")) {
            chestmode = "§aNormal";
        }

        scoreboard.updateEntire(
                plugin.customization.scoreboard_title,
                "§8ID: #" + getOyunKodu(),
                " ",
                "§fSonraki Olay:",
                " ",
                " ",
                "§fOyuncular: " + ChatColor.GREEN + (players.size() - spectators.size()),
                " ",
                "§fHarita: §a" + getArenaName(),
                "§fMod: " + chestmode,
                " ",
                "§ewww.uniocraft.com");

        fillChests();

        long currentTime = System.currentTimeMillis() / 1000L;
        plugin.sqlManager.updateGameData("tarih", String.valueOf(currentTime), oyunKodu, 0L);

        // Sunucu adını klasörden getir (Not: makine ya da klasör değiştirirsen bunu da değiştirmen lazım.)
        plugin.sqlManager.updateGameData("serverid", Bukkit.getWorldContainer().getAbsolutePath().split("/")[4], oyunKodu, 0L);

        // Oyuncu listesini güncelle
        StringBuilder sb = new StringBuilder();
        for (Player player : getPlayers()) {
            sb.append(player.getName() + ", ");
        }
        String playerList = sb.toString();
        Pattern pattern = Pattern.compile(", $");
        Matcher matcher = pattern.matcher(playerList);
        playerList = matcher.replaceAll("");
        plugin.sqlManager.updateGameData("oyunculistesi", playerList, oyunKodu, 0L);

        if (this.refillTimes != null) {
            setNextEvent("Refill");
        } else {
            setNextEvent("Hell");
        }
        setCurrentEvent("Game");

        this.gameManagerTask();
    }

    private void gameManagerTask() {
        tasks[1] = new BukkitRunnable() {
            int seconds = gameLength;
            int nextEventTime = Utils.getHighest(refillTimes, seconds);

            public void run() {

                if (Utils.getHighest(refillTimes, seconds) != 0) {
                    setNextEvent("Refill");
                } else {
                    setNextEvent("Hell");
                }

                String eventNameFormatted = "";
                if (getNextEvent().equalsIgnoreCase("Refill")) {
                    eventNameFormatted = "Yenilenme";
                } else if (getNextEvent().equalsIgnoreCase("Hell")) {
                    eventNameFormatted = "§cCehennem";
                }

                if (getArenaState() != ArenaState.ENDING) {

                    String nextEventTimeFormatted = String.format("%02d:%02d", (seconds - nextEventTime) / 60, (seconds - nextEventTime) % 60);
                    String nextEvent = ChatColor.GREEN + eventNameFormatted + " " + nextEventTimeFormatted;
                    scoreboard.update("§fSonraki Olay:", nextEvent, true, 0);

                    if (seconds == nextEventTime) {
                        if (getNextEvent().equalsIgnoreCase("Hell")) {
                            for (Player p : getPlayers()) {
                                if (!getCurrentEvent().equalsIgnoreCase("Hell")) {
                                    p.sendMessage("§4§lBu oyun haddinden fazla sürdü! Herobrine'ın hiddeti üzerinizde olsun!");
                                    scoreboard.update("§fSonraki Olay", ChatColor.RED + "Cehennem", true, 0);
                                }

                                SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
                                if (!spectators.contains(swPlayer)) {
                                    p.setFireTicks(1800);
                                    p.damage(2);
                                }

                            }
                            setCurrentEvent("Hell");
                            return;
                        }

                        if (getNextEvent().equalsIgnoreCase("Refill")) {

                            for (int i : refillTimes) {
                                if (seconds == i) {
                                    fillChests();
                                    for (Player p : getPlayers()) {
                                        p.sendMessage(plugin.customization.messages.get("Chests-Refill"));
                                    }
                                    break;
                                }
                            }
                        }

                        nextEventTime = Utils.getHighest(refillTimes, seconds);
                    }
                }

                for (Player p : getPlayers()) {
                    if (!cuboid.contains(p.getLocation())) {
                        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
                        if (spectators.contains(swPlayer)) {
                            p.sendMessage(plugin.customization.messages.get("Arena-Borders-Leave"));
                            p.teleport((Arena.this.spectatorsLocation != null) ? Arena.this.spectatorsLocation : Arena.this.cuboid.getRandomLocation());
                        } else {
                            if (p.getLocation().getBlockY() < cuboid.getLowerY()) continue;

                            if (!getArenaState().isAvailable()) {
                                p.sendMessage(plugin.customization.messages.get("Arena-Borders-Outside"));
                                p.damage(2);
                            }
                        }
                    }
                }

                seconds--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void killPlayer(SWOnlinePlayer player, SWOfflinePlayer killer) {
        Player p = player.getPlayer();
        spectators.add(player);
        plugin.bukkitPlayerManager.preparePlayer(p, "spectating");

        if (spectatorsLocation != null) {
            p.teleport(spectatorsLocation);
        } else {
            p.teleport(cuboid.getRandomLocation());
        }

        scoreboard.update("Oyuncular", ChatColor.WHITE + "Oyuncular: " + ChatColor.GREEN + (players.size() - spectators.size()), false, 6);
        JSONMessage.create(plugin.customization.prefix + "§cÖldün! §aŞansını bir daha denemek ister misin? §b§lBuraya tıkla!").tooltip("Yeni bir oyuna girmek için hemen tıkla!").runCommand("/yenioyunagir").send(p);

        if (killer != null) {
            killers.put(killer.getName(), killers.containsKey(killer.getName()) ? (killers.get(killer.getName()) + 1) : 1);
        }

        String deathMessage = plugin.bukkitPlayerManager.getDeathMessage(player.getName(), killer != null ? killer.getName() : null);
        for (Player arenaPlayer : getPlayers()) {
            arenaPlayer.sendMessage(deathMessage);
        }

        Team team = getTeam(player);
        if (team != null) {
            team.removePlayer(p);

            String message = plugin.customization.messages.get("Team-Player-Eliminate").replace("%player%", plugin.bukkitPlayerManager.disguisedPlayers.containsKey(p.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(p.getName()) : p.getName()).replace("%teamsize%", String.valueOf(team.getSize())).replace("%maxteamsize%", String.valueOf(teamSize));
            for (OfflinePlayer op : team.getPlayers()) {
                if (op instanceof Player) {
                    ((Player) op).sendMessage(message);
                }
            }
            if (team.getSize() == 0) {
                checkFinish();
            }
        } else {
            checkFinish();
        }
    }

    private void checkFinish() {
        if (this.getAliveTeams().size() < 2) {
            this.end();
        }
    }

    public void quitPlayer(String playerName) {
        SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(playerName);
        Player p = plugin.playerManager.getSWOnlinePlayer(playerName).getPlayer();

        if (swPlayer.getActionBarTask() != null) {
            swPlayer.cancelActionBarTask();
        }

        Team team = getTeam(swPlayer);
        if (team != null) {
            TeamData teamData = getTeamData(team);
            if (team.getPlayers().size() <= 0) {
                teamData.setTeamLeader(null);
                teamData.setTeamReserved(false);
            }
        }

        if (!isAvailable() && !spectators.contains(swPlayer)
                && !swPlayer.getLastAttacker().isEmpty()
                && (System.currentTimeMillis() - swPlayer.getLastAttackedTime()) / 1000L <= 15L) {

            SWOfflinePlayer lastAttacker = plugin.playerManager.getSWPlayer(swPlayer.getLastAttacker());

            if (!lastAttacker.getName().equals(playerName)) {
                plugin.playerManager.giveStat(lastAttacker, "kills", 1);
                int coinsToKiller = 0;
                if (plugin.permission.playerHas("world", lastAttacker.getName(), "rank.vip")) {
                    coinsToKiller = plugin.config.coinsPerKill * 2;
                } else {
                    coinsToKiller = plugin.config.coinsPerKill;
                }
                plugin.playerManager.giveCoin(lastAttacker, coinsToKiller);
                plugin.playerManager.giveStat(swPlayer, "deaths", 1);

                if (lastAttacker.getSWOnlinePlayer() != null && lastAttacker.getSWOnlinePlayer().getArena() != null && swPlayer.getArena() != null && lastAttacker.getSWOnlinePlayer().getArena().equals(swPlayer.getArena())) {
                    String displayName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(playerName) ? plugin.bukkitPlayerManager.disguisedPlayers.get(playerName) : playerName;
                    String killedMessage = this.plugin.customization.messages.get("Player-Kill").replace("%target%", displayName).replace("%coins%", String.valueOf(coinsToKiller));
                    Player killer = lastAttacker.getSWOnlinePlayer().getPlayer();
                    killer.sendMessage(killedMessage);
                    killer.playSound(killer.getLocation(), this.plugin.NOTE_PLING, 1.0f, 1.0f);
                    killers.put(killer.getName(), killers.containsKey(killer.getName()) ? (killers.get(killer.getName()) + 1) : 1);

                    String deathMessage = plugin.bukkitPlayerManager.getDeathMessage(playerName, killer.getName());
                    for (Player arenaPlayer : getPlayers()) {
                        arenaPlayer.sendMessage(deathMessage);
                    }

                    if (team != null) {
                        String message = plugin.customization.messages.get("Team-Player-Eliminate").replace("%player%", displayName).replace("%teamsize%", String.valueOf(team.getSize())).replace("%maxteamsize%", String.valueOf(teamSize));
                        for (OfflinePlayer op : team.getPlayers()) {
                            if (op instanceof Player) {
                                ((Player) op).sendMessage(message);
                            }
                        }
                    }
                }
            }
        }

        swPlayer.setArena(null);
        swPlayer.setLastAttackedTime(0);
        swPlayer.setLastAttacker("");

        if (team != null && team.getPlayers().contains(p)) {
            team.removePlayer(p);
        }
        players.remove(swPlayer);

        if (!spectators.contains(swPlayer)) {
            if (!isAvailable()) {
                ItemStack[] contents = p.getInventory().getContents();
                int length = contents.length;
                for (int i = 0; i < length; ++i) {
                    final ItemStack it = contents[i];
                    if (it != null) {
                        p.getWorld().dropItemNaturally(p.getLocation(), it);
                    }
                }
            }
        }

        p.teleport(plugin.lobbyLocation);
        plugin.bukkitPlayerManager.preparePlayer(p, "quit");

        String displayName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(playerName) ? plugin.bukkitPlayerManager.disguisedPlayers.get(playerName) : playerName;
        if (spectators.contains(swPlayer)) {
            spectators.remove(swPlayer);
            return;
        } else {
            List<Player> arenaPlayers = getPlayers();
            String message = plugin.customization.messages.get("Player-Leave-Arena").replace("%player%", displayName).replace("%players%", String.valueOf(players.size() - spectators.size())).replace("%maxplayers%", String.valueOf(teamSize * maxTeams));
            for (Player x : arenaPlayers) {
                x.sendMessage(message);
            }

            if (team != null) {
                String teamMessage = plugin.customization.messages.get("Player-Leave-Team").replace("%player%", displayName).replace("%teamsize%", String.valueOf(team.getSize())).replace("%maxteamsize%", String.valueOf(teamSize));
                for (OfflinePlayer x : team.getPlayers()) {
                    if (x instanceof Player) {
                        ((Player) x).sendMessage(teamMessage);
                    }
                }
            }

            if (getArenaState().isAvailable()) {

                votesManager.removeVotes(p);

                if (team != null && team.getSize() == 0) {
                    destroyCage(team);
                }

                if (tasks[0] != null && players.size() < teamSize * minTeams) {
                    cancelTask("countdown");
                    setArenaState(ArenaState.WAITING);
                    String cancelMessage = plugin.customization.messages.get("Countdown-Cancel");
                    for (Player x : arenaPlayers) {
                        x.sendMessage(cancelMessage);
                    }
                    scoreboard.update("§fGeri Sayım: §a", "§fGeri Sayım: §a" + lobbyCountdown + "s", false, 0);
                }

            } else if (team == null || team.getSize() == 0) {
                checkFinish();
            }

            if (getArenaState().equals(ArenaState.WAITING) || getArenaState().equals(ArenaState.STARTING)) {
                scoreboard.update("§fOyuncular: ", "§fOyuncular: " + ChatColor.GREEN + this.players.size() + "/" + teamSize * maxTeams, false, 0);
            } else {
                scoreboard.update("Oyuncular", ChatColor.WHITE + "Oyuncular: " + ChatColor.GREEN + (players.size() - spectators.size()), false, 6);
            }
        }
    }

    private void end() {
        if (this.arenaState.equals(ArenaState.ENDING)) {
            return;
        }
        cancelTasks();
        setArenaState(ArenaState.ENDING);

        plugin.sqlManager.updateGameData("tamamlandi", "1", oyunKodu, 1L);
        scoreboard.update("§fSonraki Olay", ChatColor.GREEN + "Oyun Bitti", true, 0);

        List<Player> arenaPlayers = getPlayers();
        List<Team> aliveTeams = getAliveTeams();
        List<SWOfflinePlayer> winners = new ArrayList<SWOfflinePlayer>();
        List<Player> onlineWinners = new ArrayList<Player>();
        List<Entry<String, Integer>> topKillers = getTopKillers();

        if (aliveTeams.isEmpty()) {
            this.stop();
            return;
        }

        for (Team team : aliveTeams) {
            for (OfflinePlayer op : team.getPlayers()) {
                if (op.isOnline()) {
                    onlineWinners.add(op.getPlayer());
                }
                winners.add(plugin.playerManager.getSWPlayer(op.getName()));
            }
        }


        String winner = "HATA";

        if (winners.size() == 1) {
            String winner1 = winners.get(0).getName();
            winner = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(winner1) ? plugin.bukkitPlayerManager.disguisedPlayers.get(winner1) : winner1;
        } else if (winners.size() == 2) {
            String winner1 = winners.get(0).getName();
            winner1 = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(winner1) ? plugin.bukkitPlayerManager.disguisedPlayers.get(winner1) : winner1;
            String winner2 = winners.get(1).getName();
            winner2 = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(winner2) ? plugin.bukkitPlayerManager.disguisedPlayers.get(winner2) : winner2;
            winner = winner1 + ", " + winner2;
        }

        String topkiller1 = (topKillers.get(0).getKey().contains("YOK") ? "YOK" : topKillers.get(0).getKey());
        String topkiller2 = (topKillers.get(1).getKey().contains("YOK") ? "YOK" : topKillers.get(1).getKey());
        String topkiller3 = (topKillers.get(2).getKey().contains("YOK") ? "YOK" : topKillers.get(2).getKey());

        topkiller1 = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(topkiller1) ? plugin.bukkitPlayerManager.disguisedPlayers.get(topkiller1) : topkiller1;
        topkiller2 = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(topkiller2) ? plugin.bukkitPlayerManager.disguisedPlayers.get(topkiller2) : topkiller2;
        topkiller3 = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(topkiller3) ? plugin.bukkitPlayerManager.disguisedPlayers.get(topkiller3) : topkiller3;

        String[] messages = new String[]{
                ChatColor.GREEN + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
                ChatColor.WHITE + "" + ChatColor.BOLD + "SkyWars",
                " ",
                "§eKazanan §7- §6" + winner,
                " ",
                "§e§l1. Öldüren §7- " + topkiller1 + " - " + topKillers.get(0).getValue(),
                "§6§l2. Öldüren §7- " + topkiller2 + " - " + topKillers.get(1).getValue(),
                "§c§l3. Öldüren §7- " + topkiller3 + " - " + topKillers.get(2).getValue(),
                " ",
                ChatColor.GREEN + "" + ChatColor.BOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"};

        for (Player arenaPlayer : arenaPlayers) {
            arenaPlayer.setFireTicks(0);
            for (String message : messages) {
                plugin.bukkitPlayerManager.sendCenteredMessage(arenaPlayer, message);
            }
        }

        for (Player winnerPlayer : onlineWinners) {
            String message = plugin.customization.messages.get("Arena-Finish");
            winnerPlayer.sendMessage(message);
            winnerPlayer.setHealth(winnerPlayer.getMaxHealth());
            for (String cmd : plugin.config.executed_commands_player_win) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", winnerPlayer.getName()).replace("%world%", cuboid.worldName));
            }
            TitleAPI.sendTitle(winnerPlayer, 10, 40, 10, "§aTebrikler!", "§6Oyunu kazandınız!");
        }

        for (SWOfflinePlayer winnerP : winners) {
            OfflinePlayer offlineWinner = winnerP.getPlayer();
            if (offlineWinner.isOnline()) {
                plugin.playerManager.giveStat(offlineWinner.getPlayer(), "wins", 1);
            } else {
                plugin.playerManager.giveStat(offlineWinner.getName(), "wins", 1);
            }

            int coinsForWin = 0;
            if (plugin.permission.playerHas(null, offlineWinner, "rank.vip")) {
                coinsForWin = plugin.config.coinsPerWin * 2;
            } else {
                coinsForWin = plugin.config.coinsPerWin;
            }

            plugin.playerManager.giveCoin(winnerP, coinsForWin);
            if (offlineWinner.isOnline()) {
                offlineWinner.getPlayer().sendMessage(plugin.customization.messages.get("Arena-Finish-Prize").replace("%coins%", String.valueOf(coinsForWin)));
            }
        }

        tasks[2] = new BukkitRunnable() {
            int seconds = plugin.config.celebrationLength;
            boolean fireworks = true;

            public void run() {

                if (fireworks) {
                    for (Player p : onlineWinners) {
                        fireWorkEffect(p, true);
                    }
                    fireworks = false;
                } else fireworks = true;

                if (seconds == 0) {
                    stop();
                }

                seconds--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public void stop() {
        if (this.arenaState.equals(ArenaState.ROLLBACKING)) {
            return;
        }
        this.cancelTasks();
        this.setArenaState(ArenaState.ROLLBACKING);

        for (Player p : getPlayers()) {
            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(p.getName());
            quitPlayer(p.getName());
            plugin.playerManager.sendPlayerToANewGame(swPlayer);
        }

        votesManager = null;
        cuboid = null;
        chests.clear();
        chests = null;
        players.clear();
        players = null;
        killers.clear();
        killers = null;
        tasks = null;
        spectators.clear();
        spectators = null;
        teams.clear();
        teams = null;
        init();
    }

    //TODO 3'e bölmeden kalan artık eşyaların sadece 1 tanesini koyuyorsun şu an. Bu sorunu çöz ve tümünü koy.
    public void fillChests() {
        String chestMode = votesManager.getVoted("Chests").equals("Default") ? "Normal" : ChatColor.stripColor(votesManager.getVoted("Chests"));
        List<Location> filledChests = new ArrayList<Location>();

        for (Location chest : chests.keySet()) {
            if (!chest.getBlock().getType().equals(Material.CHEST)) {
                continue;
            }

            if (filledChests.contains(chest)) {
                continue;
            }

            String chestType = chests.get(chest);
            ArrayList<ItemStack> chestContent = getChestContent(chestMode, chestType);
            if (chestContent != null) {
                if (chestType.equalsIgnoreCase("Default")) {
                    Integer chestSpawnPoint = chestSpawnPoints.get(chest); // Şu anki chestin spawnpointi
                    // Tüm chestleri çek
                    ArrayList<Chest> spawnpointChests = new ArrayList<Chest>();
                    for (Location chestPoint : chestSpawnPoints.keySet()) {
                        if (!chestPoint.getBlock().getType().equals(Material.CHEST)) {
                            continue;
                        }

                        if (chestSpawnPoint.equals(chestSpawnPoints.get(chestPoint))) {
                            spawnpointChests.add((Chest) chestPoint.getBlock().getState());
                        }
                    }

                    if (spawnpointChests.size() == 3) {
                        Chest chest1 = spawnpointChests.get(0);
                        Chest chest2 = spawnpointChests.get(1);
                        Chest chest3 = spawnpointChests.get(2);

                        Integer random1 = plugin.r.nextInt(4) + 4;
                        Integer random2 = plugin.r.nextInt(4) + 4;
                        Collections.shuffle(chestContent);
                        ArrayList<ItemStack> content1 = new ArrayList<ItemStack>(chestContent.subList(0, random1));
                        chestContent.removeAll(content1);
                        ArrayList<ItemStack> content2 = new ArrayList<ItemStack>(chestContent.subList(0, random2));
                        chestContent.removeAll(content2);

                        fillChest(chest1, content1);
                        filledChests.add(chest1.getLocation());
                        fillChest(chest2, content2);
                        filledChests.add(chest2.getLocation());
                        fillChest(chest3, chestContent);
                        filledChests.add(chest3.getLocation());
                    }
                } else {
                    fillChest((Chest) chest.getBlock().getState(), chestContent);
                    filledChests.add(chest);
                }
            }
        }
    }

    private void fillChest(Chest chest, ArrayList<ItemStack> content) {
        chest.getInventory().clear();
        boolean bowChosen = false;
        if (content.isEmpty()) return;

        for (int i = 0; i < content.size(); i++) {
            //Simple slot management
            int slot = 0;
            for (int x = 0; x < content.size(); x++) {
                if ((chest.getInventory().getItem(slot = plugin.r.nextInt(chest.getInventory().getSize()))) == null) {
                    break;
                }
            }

            if ((content.get(i).getType().equals(Material.ARROW))) {
                if (!bowChosen && !chest.getInventory().contains(Material.BOW)) {
                    chest.getInventory().setItem(slot, content.get(i));
                    ItemStack bow = new ItemStackBuilder(Material.BOW).build();
                    int yenislot = 0;
                    for (int x = 0; x < 10; x++)
                        if ((chest.getInventory().getItem(yenislot = plugin.r.nextInt(chest.getInventory().getSize()))) == null)
                            break;
                    chest.getInventory().setItem(yenislot, bow);
                    bowChosen = true;
                }
            } else if ((content.get(i).getType().equals(Material.BOW))) {
                if (!bowChosen && !chest.getInventory().contains(Material.ARROW)) {
                    chest.getInventory().setItem(slot, content.get(i));
                    ItemStack arrow = new ItemStackBuilder(Material.ARROW).setAmount(16).build();
                    int yenislot = 0;
                    for (int x = 0; x < 10; x++)
                        if ((chest.getInventory().getItem(yenislot = plugin.r.nextInt(chest.getInventory().getSize()))) == null)
                            break;
                    chest.getInventory().setItem(yenislot, arrow);
                    bowChosen = true;
                }
            } else {
                chest.getInventory().setItem(slot, content.get(i));
            }
        }
    }

    private ArrayList<ItemStack> getChestContent(String chestMode, String chestType) {
        if (chestMode.equalsIgnoreCase("Normal")) {
            if (chestType.equalsIgnoreCase("Default")) {
                ArrayList<ItemStack> chestContent = new ArrayList<ItemStack>();
                // 14 ve 20 eşya arası eşya olacak. Bunların 4'ü zırh 1'i kılıç ve 1'i blok. Gerisi rastgele. Bu yüzden 14-6 ve 20-6 arası eşya sayısı olacak aşağıdaki sayı.
                int itemAmount = plugin.r.nextInt(6) + 9; // 0-5 arası bir sayı + 9 = minimum 8 maksimum 14 olacak. Final sayının üzerine de 6 ekleyerek sandığın içindeki gerçek eşya sayısını bulabilirsin.

                chestContent.add(plugin.arenaManager.normalHelmetArmors.get(plugin.r.nextInt(plugin.arenaManager.normalHelmetArmors.size())));
                chestContent.add(plugin.arenaManager.normalChestplateArmors.get(plugin.r.nextInt(plugin.arenaManager.normalChestplateArmors.size())));
                chestContent.add(plugin.arenaManager.normalLeggingsArmors.get(plugin.r.nextInt(plugin.arenaManager.normalLeggingsArmors.size())));
                chestContent.add(plugin.arenaManager.normalBootsArmors.get(plugin.r.nextInt(plugin.arenaManager.normalBootsArmors.size())));
                chestContent.add(plugin.arenaManager.normalSwords.get(plugin.r.nextInt(plugin.arenaManager.normalSwords.size())));
                chestContent.add(plugin.arenaManager.normalBlocks.get(plugin.r.nextInt(plugin.arenaManager.normalBlocks.size())));

                for (int i = 0; i < itemAmount; i++) {
                    ItemStack item = plugin.arenaManager.defaultNormalItems.get(plugin.r.nextInt(plugin.arenaManager.defaultNormalItems.size()));
                    if (!chestContent.contains(item)) {
                        chestContent.add(item);
                    } else {
                        i--;
                    }
                }
                return chestContent;
            }

            if (chestType.equalsIgnoreCase("Mid")) {
                ArrayList<ItemStack> chestContent = new ArrayList<ItemStack>();
                int itemAmount = plugin.r.nextInt(4) + 5;

                for (int i = 0; i < itemAmount; i++) {
                    ItemStack item = plugin.arenaManager.midNormalItems.get(plugin.r.nextInt(plugin.arenaManager.midNormalItems.size()));
                    if (!chestContent.contains(item)) {
                        chestContent.add(item);
                    } else {
                        i--;
                    }
                }
                return chestContent;
            }
        }

        if (chestMode.equalsIgnoreCase("Insane")) {
            if (chestType.equalsIgnoreCase("Default")) {
                ArrayList<ItemStack> chestContent = new ArrayList<ItemStack>();
                // 14 ve 20 eşya arası eşya olacak. Bunların 4'ü zırh 1'i kılıç ve 1'i blok. Gerisi rastgele. Bu yüzden 14-6 ve 20-6 arası eşya sayısı olacak aşağıdaki sayı.
                int itemAmount = plugin.r.nextInt(6) + 9; // 0-5 arası bir sayı + 9 = minimum 8 maksimum 14 olacak. Final sayının üzerine de 6 ekleyerek sandığın içindeki gerçek eşya sayısını bulabilirsin.

                chestContent.add(plugin.arenaManager.insaneHelmetArmors.get(plugin.r.nextInt(plugin.arenaManager.insaneHelmetArmors.size())));
                chestContent.add(plugin.arenaManager.insaneChestplateArmors.get(plugin.r.nextInt(plugin.arenaManager.insaneChestplateArmors.size())));
                chestContent.add(plugin.arenaManager.insaneLeggingsArmors.get(plugin.r.nextInt(plugin.arenaManager.insaneLeggingsArmors.size())));
                chestContent.add(plugin.arenaManager.insaneBootsArmors.get(plugin.r.nextInt(plugin.arenaManager.insaneBootsArmors.size())));
                chestContent.add(plugin.arenaManager.insaneSwords.get(plugin.r.nextInt(plugin.arenaManager.insaneSwords.size())));
                chestContent.add(plugin.arenaManager.insaneBlocks.get(plugin.r.nextInt(plugin.arenaManager.insaneBlocks.size())));

                for (int i = 0; i < itemAmount; i++) {
                    ItemStack item = plugin.arenaManager.defaultInsaneItems.get(plugin.r.nextInt(plugin.arenaManager.defaultInsaneItems.size()));
                    if (!chestContent.contains(item)) {
                        chestContent.add(item);
                    } else {
                        i--;
                    }
                }
                return chestContent;
            }

            if (chestType.equalsIgnoreCase("Mid")) {
                ArrayList<ItemStack> chestContent = new ArrayList<ItemStack>();
                int itemAmount = plugin.r.nextInt(4) + 5;

                for (int i = 0; i < itemAmount; i++) {
                    ItemStack item = plugin.arenaManager.midInsaneItems.get(plugin.r.nextInt(plugin.arenaManager.midInsaneItems.size()));
                    if (!chestContent.contains(item)) {
                        chestContent.add(item);
                    } else {
                        i--;
                    }
                }
                return chestContent;
            }
        }
        return null;
    }

    private void fireWorkEffect(Player p, boolean instantBlow) {
        if (!plugin.config.fireworksEnabled) return;
        final Firework firework = p.getWorld().spawn(p.getLocation().add(0, 1, 0), Firework.class);
        FireworkMeta fwm = firework.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(plugin.r.nextBoolean()).withColor(Color.fromBGR(plugin.r.nextInt(256), plugin.r.nextInt(256), plugin.r.nextInt(256))).withFade(Color.fromBGR(plugin.r.nextInt(256), plugin.r.nextInt(256), plugin.r.nextInt(256))).with(Type.values()[plugin.r.nextInt(Type.values().length)]).trail(plugin.r.nextBoolean()).build();
        fwm.addEffect(effect);
        firework.setFireworkMeta(fwm);

        if (instantBlow) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                public void run() {
                    firework.detonate();
                }
            }, 2);
        }
    }

    // Non Main Gameplay Functions
    private void resetScoreboard() {
        scoreboard = plugin.scoreboardManager.createScoreboard(
                new String[]{
                        "§8ID: #" + getOyunKodu(),
                        " ",
                        "§fGeri Sayım: §a" + lobbyCountdown + "s",
                        " ",
                        "§fOyuncular: §a" + players.size() + "/" + teamSize * maxTeams,
                        " ",
                        "§fHarita: §a" + arenaName,
                        " ",
                        "§ewww.uniocraft.com"
                });
    }

    private void resetOyunKodu() {
        plugin.sqlManager.generateOyunKodu(this);
    }

    // TODO Make here more readable.
    private void resetLocations() {
        FileConfiguration locationsConfig = plugin.configManager.getArenaLocations(arenaName);

        // Set spectator spawnpoint.
        if (locationsConfig.contains("Spectators-Spawnpoint")) {
            spectatorsLocation = Utils.getLocationFromString(locationsConfig.getString("Spectators-Spawnpoint"));
        } else {
            spectatorsLocation = null;
        }

        // Register chests
        String[] chestsSpliter = locationsConfig.getString("Chests").replace("[", "").replace("]", "").split(", ");
        for (String chestLocation : chestsSpliter) {
            String[] chestLocationSpliter = chestLocation.split(":");
            if (chestLocationSpliter.length >= 6) {
                Location chest = new Location(Bukkit.getWorld(cuboid.worldName), Integer.valueOf(chestLocationSpliter[0]), Integer.valueOf(chestLocationSpliter[1]), Integer.valueOf(chestLocationSpliter[2]));
                chests.put(chest, chestLocationSpliter[5].toLowerCase());
                if (!chestLocationSpliter[5].toLowerCase().equalsIgnoreCase("mid")) {
                    chestSpawnPoints.put(chest, Integer.valueOf(chestLocationSpliter[6]));
                }
            }
        }

        // Register spawnpoints and teams
        if ((locationsConfig.getConfigurationSection("Spawnpoints") != null) && !(locationsConfig.getConfigurationSection("Spawnpoints").getKeys(false).isEmpty())) {
            for (final String s2 : locationsConfig.getConfigurationSection("Spawnpoints").getKeys(false)) {
                if (teams.size() < 12) {
                    registerTeam(s2, Utils.getLocationFromString(locationsConfig.getString("Spawnpoints." + s2)));
                }
            }
        }
    }

    // TODO Make here more readable.
    // TODO Recode team register system.
    public void registerTeam(String spawnpoint, Location location) {
        Team team = scoreboard.registerTeam(spawnpoint);
        team.setAllowFriendlyFire(false);
        setPrefix(team);

        teams.put(team, new TeamData(location));
    }

    // TODO Make here more readable.
    // TODO Recode team register system.
    private void setPrefix(Team team) {
        String prefix1 = ChatColor.translateAlternateColorCodes('&', "&a[A] ");
        String prefix2 = ChatColor.translateAlternateColorCodes('&', "&b[B] ");
        String prefix3 = ChatColor.translateAlternateColorCodes('&', "&c[C] ");
        String prefix4 = ChatColor.translateAlternateColorCodes('&', "&d[D] ");
        String prefix5 = ChatColor.translateAlternateColorCodes('&', "&e[E] ");
        String prefix6 = ChatColor.translateAlternateColorCodes('&', "&2[F] ");
        String prefix7 = ChatColor.translateAlternateColorCodes('&', "&3[G] ");
        String prefix8 = ChatColor.translateAlternateColorCodes('&', "&5[H] ");
        String prefix9 = ChatColor.translateAlternateColorCodes('&', "&5[I] ");
        String prefix10 = ChatColor.translateAlternateColorCodes('&', "&6[J] ");
        String prefix11 = ChatColor.translateAlternateColorCodes('&', "&7[K] ");
        String prefix12 = ChatColor.translateAlternateColorCodes('&', "&9[L] ");

        ArrayList<String> usedPrefixes = new ArrayList<String>();
        for (Team t : teams.keySet()) {
            if (t.getPrefix().contains(prefix1)) {
                usedPrefixes.add(prefix1);
            }
            if (t.getPrefix().contains(prefix2)) {
                usedPrefixes.add(prefix2);
            }
            if (t.getPrefix().contains(prefix3)) {
                usedPrefixes.add(prefix3);
            }
            if (t.getPrefix().contains(prefix4)) {
                usedPrefixes.add(prefix4);
            }
            if (t.getPrefix().contains(prefix5)) {
                usedPrefixes.add(prefix5);
            }
            if (t.getPrefix().contains(prefix6)) {
                usedPrefixes.add(prefix6);
            }
            if (t.getPrefix().contains(prefix7)) {
                usedPrefixes.add(prefix7);
            }
            if (t.getPrefix().contains(prefix8)) {
                usedPrefixes.add(prefix8);
            }
            if (t.getPrefix().contains(prefix9)) {
                usedPrefixes.add(prefix9);
            }
            if (t.getPrefix().contains(prefix10)) {
                usedPrefixes.add(prefix10);
            }
            if (t.getPrefix().contains(prefix11)) {
                usedPrefixes.add(prefix11);
            }
            if (t.getPrefix().contains(prefix12)) {
                usedPrefixes.add(prefix12);
            }
        }

        if (!usedPrefixes.contains(prefix1)) {
            team.setPrefix(prefix1);
            return;
        }
        if (!usedPrefixes.contains(prefix2)) {
            team.setPrefix(prefix2);
            return;
        }
        if (!usedPrefixes.contains(prefix3)) {
            team.setPrefix(prefix3);
            return;
        }
        if (!usedPrefixes.contains(prefix4)) {
            team.setPrefix(prefix4);
            return;
        }
        if (!usedPrefixes.contains(prefix5)) {
            team.setPrefix(prefix5);
            return;
        }
        if (!usedPrefixes.contains(prefix6)) {
            team.setPrefix(prefix6);
            return;
        }
        if (!usedPrefixes.contains(prefix7)) {
            team.setPrefix(prefix7);
            return;
        }
        if (!usedPrefixes.contains(prefix8)) {
            team.setPrefix(prefix8);
            return;
        }
        if (!usedPrefixes.contains(prefix9)) {
            team.setPrefix(prefix9);
            return;
        }
        if (!usedPrefixes.contains(prefix10)) {
            team.setPrefix(prefix10);
            return;
        }
        if (!usedPrefixes.contains(prefix11)) {
            team.setPrefix(prefix11);
            return;
        }
        if (!usedPrefixes.contains(prefix12)) {
            team.setPrefix(prefix12);
            return;
        }
    }

    public void removeTeam(Team team) {
        teams.remove(team);
    }

    // TODO Make here more readable.
    // TODO Recode team register system.
    public Team getAvailableTeam(int capacity) {
        for (Team team : teams.keySet()) {
            TeamData teamData = getTeamData(team);
            if (team.getSize() + capacity <= teamSize) {
                if (!teamData.isTeamReserved()) {
                    return team;
                }
            }
        }
        return null;
    }

    private void buildCage(Cage cage, HashMap<Location, Integer> locations) {
        /*
         * 0 = ceiling
         * 1 = ceilingBorder
         * 2 = higherMiddle
         * 3 = higherMiddleBorder
         * 4 = middle
         * 5 = middleBorder
         * 6 = lowerMiddle
         * 7 = lowerMiddleBorder
         * 8 = floorBorder
         * 9 = floor
         */
        ItemStack[] cageParts = cage.cageParts;
        for (final Location location : locations.keySet()) {
            location.getBlock().setTypeIdAndData(cageParts[locations.get(location)].getTypeId(), cageParts[locations.get(location)].getData().getData(), true);
        }
    }

    //TODO Recode
    private List<Entry<String, Integer>> getTopKillers() {
        while (killers.size() < 3) killers.put("YOK" + (plugin.r.nextInt(5) + 1), 0);
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(killers.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        return list;
    }

    private void cancelTasks() {
        this.cancelTask("countdown");
        this.cancelTask("game");
        this.cancelTask("ending");
    }

    private void cancelTask(String taskName) {
        int id = -1;
        if (taskName.equalsIgnoreCase("countdown")) {
            id = 0;
        } else if (taskName.equalsIgnoreCase("game")) {
            id = 1;
        } else if (taskName.equalsIgnoreCase("ending")) {
            id = 2;
        }
        if (id != -1) {
            if (tasks[id] != null) {
                tasks[id].cancel();
            }
            tasks[id] = null;
        }
    }

    public void destroyCage(Team team) {
        Set<Location> cage = null;
        if (getArenaMode().equalsIgnoreCase("solo")) {
            cage = getTeamData(team).getSmallCage().keySet();
        } else {
            cage = getTeamData(team).getLargeCage().keySet();
        }

        for (Location location : cage) {
            location.getBlock().setType(Material.AIR);
        }
    }

    private void destroyCages() {
        for (Team team : teams.keySet()) {
            destroyCage(team);
        }
    }

    // Private Get, Set Functions

    public HashMap<Team, TeamData> getTeams() {
        return teams;
    }

    private TeamData getTeamData(Team team) {
        return teams.get(team);
    }

    // Public Get, Set Functions
    public Team getTeam(SWOnlinePlayer p) {
        return players.get(p);
    }

    public String getArenaName() {
        return arenaName;
    }

    public String getArenaMode() {
        return gameMode;
    }

    public ArenaState getArenaState() {
        return arenaState;
    }

    public void setArenaState(ArenaState state) {
        this.arenaState = state;
    }

    public boolean isAvailable() {
        return arenaState.isAvailable();
    }

    public boolean isJoinable() {
        if (enabled) {
            if (teams.size() >= maxTeams && players.size() + 1 <= teamSize * maxTeams) {
                return arenaState.isAvailable();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Integer getCurrentTeamAmount() {
        return teams.size();
    }

    public Integer getMinTeamAmount() {
        return minTeams;
    }

    public Integer getMaxTeamAmount() {
        return maxTeams;
    }

    public String getCurrentEvent() {
        return currentEvent;
    }

    private void setCurrentEvent(String currentEvent) {
        this.currentEvent = currentEvent;
    }

    public String getNextEvent() {
        return nextEvent;
    }

    private void setNextEvent(String nextEvent) {
        this.nextEvent = nextEvent;
    }

    public CustomScoreboard getScoreboard() {
        return scoreboard;
    }

    public void setScoreboardName(String newName) {
        scoreboard.setName(newName);
    }

    public List<Player> getPlayers() {
        return Utils.getPlayers(players.keySet());
    }

    public ArrayList<SWOnlinePlayer> getSpectators() {
        return spectators;
    }

    public void setSpectatorsLocation(Location loc) {
        this.spectatorsLocation = loc;
    }

    // TODO Make it more readable
    public List<Team> getAliveTeams() {
        final ArrayList<Team> list = new ArrayList<Team>();
        for (final Team team : this.teams.keySet()) {
            if (team.getSize() > 0) {
                list.add(team);
            }
        }
        return list;
    }

    public String getArenaWorldName() {
        return cuboid.worldName;
    }

    public Cuboid getCuboid() {
        return cuboid;
    }

    public int getOyunKodu() {
        return this.oyunKodu;
    }

    public void setOyunKodu(int oyunKodu) {
        this.oyunKodu = oyunKodu;
    }

    public Integer getPlayerSizePerTeam() {
        return teamSize;
    }

    public Integer getLobbyCountdown() {
        return lobbyCountdown;
    }

    public void setLobbyCountdown(Integer in) {
        this.lobbyCountdown = in;
    }

    public Integer getGameLength() {
        return gameLength;
    }

    public void setGameLength(Integer in) {
        this.gameLength = in;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setArenaState(enabled ? ArenaState.WAITING : ArenaState.DISABLED);
        File file = new File(plugin.getDataFolder() + "/arenas/" + this.getArenaName(), "settings.yml");
        FileConfiguration editor = YamlConfiguration.loadConfiguration(file);
        editor.set("enabled", enabled);
        try {
            editor.save(file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setTeamSize(Integer in) {
        this.teamSize = in;
    }

    public void setMinTeams(Integer in) {
        this.minTeams = in;
    }

    public void setMaxTeams(Integer in) {
        this.maxTeams = in;
    }

    public void updateGameMode() {
        this.scoreboard.update(plugin.customization.scoreboard.get("Mode"), this.getArenaMode(), true, 0);
    }

    public VotesManager getVoteManager() {
        return votesManager;
    }

    public void setVoteManager(VotesManager votesManager) {
        this.votesManager = votesManager;
    }

    public HashMap<Location, String> getChests() {
        return chests;
    }

    public void setChests(HashMap<Location, String> chests) {
        this.chests = chests;
    }
}
