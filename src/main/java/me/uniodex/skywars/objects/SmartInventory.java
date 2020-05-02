package me.uniodex.skywars.objects;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO Find out what it does and recode
public class SmartInventory {

    private String mainName;
    private HashMap<Integer, Inventory> inventories = new HashMap<Integer, Inventory>();

    private Skywars plugin;

    public SmartInventory(Skywars plugin, String mainName) {
        this.plugin = plugin;
        this.mainName = mainName + ": ";
    }

    public int addInventory(String name) {
        int id = inventories.size();

        Inventory inv = Bukkit.createInventory(null, 54, mainName + name);
        Utils.cageInventory(inv, false);

        if (id != 0) {
            inv.setItem(18, this.plugin.clickableItemManager.getPreviousItem());
            inv.setItem(27, this.plugin.clickableItemManager.getPreviousItem());
            Inventory previousInventory = inventories.get(id - 1);
            previousInventory.setItem(26, this.plugin.clickableItemManager.getNextItem());
            previousInventory.setItem(35, this.plugin.clickableItemManager.getNextItem());
        }

        inventories.put(id, inv);
        return id;
    }

    public void setItem(int id, int slot, ItemStack item) {
        inventories.get(id).setItem(slot, item);
    }

    public boolean addItem(int id, ItemStack item) {
        Inventory inv = inventories.get(id);
        int slot = getEmptySlot(id);
        if (slot == -1) return false;
        inv.setItem(slot, item);
        return true;
    }

    public void removeItem(int id, int slot) {
        setItem(id, slot, new ItemStack(Material.AIR));
        organize(id);
    }

    public ItemStack getItem(int id, int slot) {
        return inventories.get(id).getItem(slot);
    }

    public void clear(int id) {
        Inventory inv = inventories.get(id);
        for (int slot : plugin.smartSlots) inv.setItem(slot, new ItemStack(Material.AIR));
    }

    public void organize(int id) {
        Inventory inv = inventories.get(id);
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (int i : plugin.smartSlots) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                items.add(item);
                inv.setItem(i, new ItemStack(Material.AIR));
            }
        }
        int i = 0;
        for (ItemStack item : items) {
            inv.setItem(plugin.smartSlots[i], item);
            i++;
        }
    }

    public int getEmptySlot(int id) {
        Inventory inv = inventories.get(id);
        for (int i : plugin.smartSlots) if (inv.getItem(i) == null) return i;
        return -1;
    }

    public List<ItemStack> getContents(int id) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        Inventory inv = inventories.get(id);
        for (int slot : plugin.smartSlots) {
            ItemStack item = inv.getItem(slot);
            if (item != null) items.add(item);
        }
        return items;
    }

    public boolean handleClick(Player p, ItemStack clicked, Inventory inv) {
        if (!Utils.compareItem(clicked, this.plugin.clickableItemManager.getNextItem()) && !Utils.compareItem(clicked, this.plugin.clickableItemManager.getPreviousItem()))
            return false;
        int id = 0;
        for (int i = 0; i < inventories.size(); i++) if (inventories.get(id = i).getName().equals(inv.getName())) break;
        if (Utils.compareItem(clicked, this.plugin.clickableItemManager.getNextItem())) {
            p.openInventory(inventories.get(id + 1));
        } else p.openInventory(inventories.get(id - 1));
        return true;
    }

    public int getSize() {
        return inventories.size();
    }

    public String getName() {
        return mainName;
    }

    public void open(Player p) {
        p.openInventory(inventories.get(0));
    }

}
