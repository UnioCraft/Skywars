package me.uniodex.skywars.objects;

import org.bukkit.inventory.ItemStack;

public class Trail extends Item {

    public ItemStack trailType;

    public Trail(String trailId, String trailName, ItemStack trailIcon, String trailItemInfo, String trailRarity, int trailCost, String trailPermission, ItemStack trailType) {
        this.itemId = trailId;
        this.itemName = trailName;
        this.itemType = "trail";
        this.itemIcon = trailIcon;
        this.itemInfo = trailItemInfo;
        this.rarity = trailRarity;
        this.cost = trailCost;
        this.permission = trailPermission;
        this.trailType = trailType;
    }
}
