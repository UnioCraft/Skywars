package me.uniodex.skywars.objects;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Kit extends Item {

    public ArrayList<ItemStack> items = new ArrayList<ItemStack>();

    public Kit(String kitId, String kitName, ItemStack kitIcon, String kitItemInfo, String kitRarity, int kitCost, String kitPermission, ArrayList<ItemStack> items) {
        this.itemId = kitId;
        this.itemName = kitName;
        this.itemType = "kit";
        this.itemIcon = kitIcon;
        this.itemInfo = kitItemInfo;
        this.rarity = kitRarity;
        this.cost = kitCost;
        this.permission = kitPermission;
        this.items = items;
    }
}
