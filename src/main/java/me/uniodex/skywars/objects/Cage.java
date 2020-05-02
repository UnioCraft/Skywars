package me.uniodex.skywars.objects;

import org.bukkit.inventory.ItemStack;

public class Cage extends Item {

    public ItemStack[] cageParts;

    public Cage(String cageId, String cageName, ItemStack cageIcon, String cageItemInfo, String cageRarity, int cageCost, String cagePermission, ItemStack[] cageParts) {
        this.itemId = cageId;
        this.itemName = cageName;
        this.itemType = "cage";
        this.itemIcon = cageIcon;
        this.itemInfo = cageItemInfo;
        this.rarity = cageRarity;
        this.cost = cageCost;
        this.permission = cagePermission;
        this.cageParts = cageParts;
    }
}
