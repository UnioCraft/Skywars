package me.uniodex.skywars.arena;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.player.SWOnlinePlayer;
import me.uniodex.skywars.utils.ItemStackBuilder;
import me.uniodex.skywars.utils.Utils;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.InventoryMenuBuilder;
import me.uniodex.skywars.utils.packages.menubuilder.inventory.ItemListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Recode
public class VotesManager {

    private HashMap<String, Entry> entries;

    public VotesManager(Skywars plugin) {
        entries = new HashMap<String, Entry>();
        ArrayList<String> chestList = new ArrayList<String>();
        chestList.add(ChatColor.GREEN + "Normal");
        chestList.add(ChatColor.GREEN + "Insane");
        entries.put("Chests", new Entry(plugin, "Chests", new ItemStack(Material.CHEST), chestList, false));
    }

    private void vote(Player p, String category, String option) {
        entries.get(category).voters.put(p.getName(), option);
    }

    public void removeVotes(Player p) {
        for (Entry entry : entries.values()) entry.voters.remove(p.getName());
    }

    public void openEntry(Player p, String category) {
        p.openInventory(entries.get(category).menu.build());
    }

    public Inventory getEntry(String category) {
        return entries.get(category).menu.build();
    }

    public int getVotes(String category, String option) {
        int i = 0;
        HashMap<String, String> voters = entries.get(category).voters;
        for (String player : voters.keySet()) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                if (p.hasPermission("rank.vip")) {
                    if (voters.get(player).equals(option)) i += 2;
                } else {
                    if (voters.get(player).equals(option)) i += 1;
                }
            }
        }
        return i;
    }

    public String getVoted(String category) {
        Entry entry = entries.get(category);
        String voted = "Default";
        int votes = 0;
        for (String option : entry.options) {
            int optionVotes = getVotes(category, option);
            if (optionVotes > votes) {
                voted = option;
                votes = optionVotes;
            }
        }
        return voted;
    }

    public void reset() {
        for (Entry entry : entries.values()) entry.voters.clear();
    }

    private class Entry {

        InventoryMenuBuilder menu;
        HashMap<String, String> voters;
        List<String> options;

        private Entry(Skywars plugin, String category, ItemStack item, List<String> options, boolean color) {
            voters = new HashMap<String, String>();
            this.options = new ArrayList<String>();
            for (String string : options) this.options.add(ChatColor.stripColor(string));
            menu = new InventoryMenuBuilder(Utils.getInventorySize(options.size()), "§1§lSandık Türü Seçin:");
            //TODO change cage menu = plugin.cageInventory(menu, true);
            boolean set1 = false;
            for (int i = 0; i < options.size(); i++) {
                final int fnlI = i;
                if (!set1) {
                    menu.withItem(i + 3, new ItemStackBuilder(item).setName(color ? ChatColor.AQUA + options.get(i) : options.get(i)).build(), new ItemListener() {
                        @Override
                        public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());
                            if (swPlayer.hasCooldown("SETTINGS_VOTE", 5, true)) {
                                return;
                            }
                            swPlayer.addCooldown("SETTINGS_VOTE", 5);

                            String category = "Chests";
                            String option = ChatColor.stripColor(options.get(fnlI));
                            vote(player, category, option);
                            String displayName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(player.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(player.getName()) : player.getName();
                            String message = plugin.customization.messages.get("Player-Vote").replace("%player%", displayName).replace("%option%", option).replace("%votes%", String.valueOf(getVotes(category, option)));
                            player.closeInventory();
                            for (Player x : swPlayer.getArena().getPlayers()) x.sendMessage(message);
                        }
                    }, InventoryMenuBuilder.ALL_CLICK_TYPES);
                    set1 = true;
                } else {
                    menu.withItem(i + 4, new ItemStackBuilder(item).setName(color ? ChatColor.AQUA + options.get(i) : options.get(i)).build(), new ItemListener() {
                        @Override
                        public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                            SWOnlinePlayer swPlayer = plugin.playerManager.getSWOnlinePlayer(player.getName());
                            if (swPlayer.hasCooldown("SETTINGS_VOTE", 5, true)) {
                                return;
                            }
                            swPlayer.addCooldown("SETTINGS_VOTE", 5);

                            String category = "Chests";
                            String option = ChatColor.stripColor(options.get(fnlI));
                            vote(player, category, option);
                            String displayName = plugin.bukkitPlayerManager.disguisedPlayers.containsKey(player.getName()) ? plugin.bukkitPlayerManager.disguisedPlayers.get(player.getName()) : player.getName();
                            String message = plugin.customization.messages.get("Player-Vote").replace("%player%", displayName).replace("%option%", option).replace("%votes%", String.valueOf(getVotes(category, option)));
                            player.closeInventory();
                            for (Player x : swPlayer.getArena().getPlayers()) x.sendMessage(message);
                        }
                    }, InventoryMenuBuilder.ALL_CLICK_TYPES);
                }
            }
        }

    }

}
