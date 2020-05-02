package me.uniodex.skywars.objects;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ChestType {

    public String name;
    public ArrayList<ItemStack> items;
    public int minItems;
    public int maxItems;
    public SmartInventory editor;

    public ChestType(Skywars plugin, String name, ArrayList<ItemStack> items, int minItems, int maxItems) {
        this.name = name;
        this.items = items;
        this.minItems = minItems;
        this.maxItems = maxItems;

        editor = new SmartInventory(plugin, ChatColor.BLUE + "Editing");
        int currentItem = 0;
        for (int i = 1; i < (Math.ceil(Double.valueOf(items.isEmpty() ? 1 : items.size()) / plugin.smartSlots.length) + 1); i++) {
            int id = editor.addInventory(ChatColor.RED + name + " #" + i);
            for (int slot : plugin.smartSlots) {
                if (currentItem >= items.size()) break;
                editor.setItem(id, slot, items.get(currentItem));
                currentItem++;
            }
            addSettings(plugin, id);
        }
    }

    public void addSettings(Skywars plugin, int id) {
        editor.setItem(id, 46, plugin.clickableItemManager.getMinusItem());
        editor.setItem(id, 47, new ItemStackBuilder(Material.PAPER).setName(ChatColor.YELLOW + "Min items: " + ChatColor.GOLD + minItems).build());
        editor.setItem(id, 48, plugin.clickableItemManager.getPlusItem());
        editor.setItem(id, 49, plugin.clickableItemManager.getSaveItem());
        editor.setItem(id, 50, plugin.clickableItemManager.getMinusItem());
        editor.setItem(id, 51, new ItemStackBuilder(Material.PAPER).setName(ChatColor.YELLOW + "Max items: " + ChatColor.GOLD + maxItems).build());
        editor.setItem(id, 52, plugin.clickableItemManager.getPlusItem());
        editor.setItem(id, 53, new ItemStackBuilder(Material.EMERALD).setName(ChatColor.GREEN + "Generate").addLore(ChatColor.GRAY + "Click to generate a new page!").build());
    }

}
