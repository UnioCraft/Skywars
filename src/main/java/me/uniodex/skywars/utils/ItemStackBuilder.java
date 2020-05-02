package me.uniodex.skywars.utils;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

    private ItemStack item;
    private ItemMeta meta;
    private EnchantmentStorageMeta emeta;
    private List<String> lore;

    public ItemStackBuilder(ItemStack item) {
        this.item = item;
        meta = item.getItemMeta();
        if (item.getType().equals(Material.ENCHANTED_BOOK)) {
            emeta = (EnchantmentStorageMeta) meta;
        } else {
            emeta = null;
        }
        lore = meta != null && meta.hasLore() ? meta.getLore() : new ArrayList<String>();
    }

    public ItemStackBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemStackBuilder setType(Material type) {
        item.setType(type);
        return this;
    }

    public ItemStackBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemStackBuilder addLore(String... l) {
        for (String x : l) lore.add(x);
        return this;
    }

    public ItemStackBuilder addEnchantment(Enchantment e, int level) {
        meta.addEnchant(e, level, true);
        return this;
    }

    public ItemStackBuilder addBookEnchantment(Enchantment e, int level) {
        emeta.addStoredEnchant(e, level, true);
        return this;
    }

    public ItemStackBuilder setDurability(int durability) {
        item.setDurability((short) durability);
        return this;
    }

    public ItemStackBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemStackBuilder replaceLore(String oldLore, String newLore) {
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(oldLore)) {
                lore.remove(i);
                lore.add(i, newLore);
                break;
            }
        }
        return this;
    }

    public ItemStack build() {
        if (!lore.isEmpty()) {
            meta.setLore(lore);
            lore.clear();
        }
        item.setItemMeta(meta);
        if (emeta != null) {
            EnchantmentStorageMeta newEMeta = (EnchantmentStorageMeta) meta;
            for (Enchantment ench : emeta.getStoredEnchants().keySet()) {
                newEMeta.addStoredEnchant(ench, emeta.getStoredEnchants().get(ench), true);
            }
            item.setItemMeta(newEMeta);
        }
        return item;
    }

}
