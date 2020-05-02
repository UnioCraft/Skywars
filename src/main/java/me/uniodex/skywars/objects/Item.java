package me.uniodex.skywars.objects;

import org.bukkit.inventory.ItemStack;

public abstract class Item {

    public String itemId; // Veritabanı, fonksiyonlar, komutlar gibi yerlerde kullanılacak olan eşya kimliği
    public String itemName; // Eşyanın son kullanıcıya gösterilecek olan ismi
    public String itemType; // Eşyanın türü (Kit, kafes vs.)
    public ItemStack itemIcon; // Eşyanın menülerde gösterilecek olan ikonu
    public String itemInfo; // Eşyanın menülerde gösterilecek olan kendine özel tanımı
    public String rarity; // Eşyanın değerlilik türü
    public int cost; // Eşyanın fiyatı
    public String permission; // Eşyanın kullanım izni (Eğer NONE ise izne gerek yoktur.)

}
