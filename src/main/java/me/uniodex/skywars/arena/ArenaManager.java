package me.uniodex.skywars.arena;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.enums.ArenaState;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Recode
public class ArenaManager {

    // Chests
    public ArrayList<ItemStack> normalHelmetArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> normalChestplateArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> normalLeggingsArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> normalBootsArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> normalSwords = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> normalBlocks = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> defaultNormalItems = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> midNormalItems = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneHelmetArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneChestplateArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneLeggingsArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneBootsArmors = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneSwords = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> insaneBlocks = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> defaultInsaneItems = new ArrayList<ItemStack>();
    public ArrayList<ItemStack> midInsaneItems = new ArrayList<ItemStack>();
    private Skywars plugin;
    private HashMap<String, Arena> arenas;

    public ArenaManager(Skywars plugin) {
        this.plugin = plugin;
        arenas = new HashMap<String, Arena>();
    }

    public void loadArenas() {
        File arenasFolder = new File(plugin.getDataFolder(), "arenas");
        if (arenasFolder.isDirectory()) {
            for (File arena : arenasFolder.listFiles()) {
                File arenaSettings = new File(plugin.getDataFolder() + "/arenas/" + arena.getName(), "settings.yml");
                File arenaLocations = new File(plugin.getDataFolder() + "/arenas/" + arena.getName(), "locations.dat");
                File arenaWorld = new File(plugin.getDataFolder() + "/arenas/" + arena.getName(), "/world/level.dat");
                if (arena.isDirectory() && arenaSettings.exists() && arenaLocations.exists() && arenaWorld.exists()) {
                    Arena newArena = new Arena(plugin, arena.getName());
                    arenas.put(arena.getName(), newArena);
                }
            }
        }
    }

    public void loadChestItems() {
        FileConfiguration config = plugin.configManager.getChestsConfig();
        for (String item : config.getStringList("Chests.Normal.Default.items")) {
            ItemStack itemStack = Utils.getItemStack(item, true, true);
            if (Utils.isHelmet(itemStack)) {
                normalHelmetArmors.add(itemStack);
            } else if (Utils.isChestplate(itemStack)) {
                normalChestplateArmors.add(itemStack);
            } else if (Utils.isLeggings(itemStack)) {
                normalLeggingsArmors.add(itemStack);
            } else if (Utils.isBoots(itemStack)) {
                normalBootsArmors.add(itemStack);
            } else if (Utils.isSword(itemStack)) {
                normalSwords.add(itemStack);
            } else {
                if (Utils.isBlock(itemStack)) {
                    normalBlocks.add(itemStack);
                }
                defaultNormalItems.add(itemStack);
            }
        }

        for (String item : config.getStringList("Chests.Normal.Mid.items")) {
            ItemStack itemStack = Utils.getItemStack(item, true, true);
            midNormalItems.add(itemStack);
        }

        for (String item : config.getStringList("Chests.Insane.Default.items")) {
            ItemStack itemStack = Utils.getItemStack(item, true, true);
            if (Utils.isHelmet(itemStack)) {
                insaneHelmetArmors.add(itemStack);
            } else if (Utils.isChestplate(itemStack)) {
                insaneChestplateArmors.add(itemStack);
            } else if (Utils.isLeggings(itemStack)) {
                insaneLeggingsArmors.add(itemStack);
            } else if (Utils.isBoots(itemStack)) {
                insaneBootsArmors.add(itemStack);
            } else if (Utils.isSword(itemStack)) {
                insaneSwords.add(itemStack);
            } else {
                if (Utils.isBlock(itemStack)) {
                    insaneBlocks.add(itemStack);
                }
                defaultInsaneItems.add(itemStack);
            }
        }

        for (String item : config.getStringList("Chests.Insane.Mid.items")) {
            ItemStack itemStack = Utils.getItemStack(item, true, true);
            midInsaneItems.add(itemStack);
        }
    }

    public HashMap<String, Arena> getArenas() {
        return arenas;
    }

    public void removeArena(String arenaName) {
        arenas.remove(arenaName);
    }

    public void registerArena(String arenaName) {
        Arena arena = new Arena(plugin, arenaName);
        arenas.put(arenaName, arena);
    }

    public Arena getArena(String arenaName) {
        return this.arenas.get(arenaName);
    }

    public Arena getAvailableArena(Integer playerCount) {
        if (plugin.serverWillStop) {
            return null;
        }

        List<Arena> available = new ArrayList<Arena>();
        for (Arena arena : arenas.values()) {
            if (arena.isEnabled() && arena.isJoinable() && arena.getCurrentTeamAmount() >= arena.getMaxTeamAmount() && (arena.getPlayers().size()) + playerCount <= (arena.getPlayerSizePerTeam() * arena.getMaxTeamAmount())) {
                available.add(arena);
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        Arena selected = available.get(0);
        for (Arena arena : available) {
            if (arena.getPlayers().size() > selected.getPlayers().size()) selected = arena;
        }
        return selected;
    }

    public boolean isTherePlayingArenas() {
        for (Arena arena : arenas.values()) {
            if (arena.getArenaState().equals(ArenaState.HELL) || arena.getArenaState().equals(ArenaState.INGAME)) {
                return true;
            }
        }
        return false;
    }
}
