package me.uniodex.skywars.utils.packages.menubuilder.inventory;

import me.uniodex.skywars.utils.packages.menubuilder.util.AccessUtil;
import me.uniodex.skywars.utils.packages.menubuilder.util.Reflection;
import org.bukkit.inventory.Inventory;

public class InventoryHelper {

    static Class<?> obcCraftInventory;
    static Class<?> obcCraftInventoryCustom;
    static Class<?> obcMinecraftInventory;

    static {
        try {
            obcCraftInventory = Reflection.getOBCClass("inventory.CraftInventory");
            obcCraftInventoryCustom = Reflection.getOBCClass("inventory.CraftInventoryCustom");
            for (Class<?> c : obcCraftInventoryCustom.getDeclaredClasses()) {
                if (c.getSimpleName().equals("MinecraftInventory")) {
                    obcMinecraftInventory = c;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeTitle(Inventory inv, String title) {
        try {
            Object minecrafInventory = AccessUtil.setAccessible(obcCraftInventory.getDeclaredField("inventory")).get(inv);
            AccessUtil.setAccessible(obcMinecraftInventory.getDeclaredField("title")).set(minecrafInventory, title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
