package me.uniodex.skywars.utils.packages.menubuilder.inventory;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.packages.menubuilder.MenuBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class InventoryMenuBuilder
        extends MenuBuilder<Inventory> {
    public static final ClickType[] ALL_CLICK_TYPES = {ClickType.LEFT, ClickType.SHIFT_LEFT, ClickType.RIGHT, ClickType.SHIFT_RIGHT, ClickType.WINDOW_BORDER_LEFT, ClickType.WINDOW_BORDER_RIGHT, ClickType.MIDDLE, ClickType.NUMBER_KEY, ClickType.DOUBLE_CLICK, ClickType.DROP, ClickType.CONTROL_DROP};
    private Inventory inventory;
    private List<ItemCallback> callbackItems = new ArrayList();

    public InventoryMenuBuilder() {
    }

    public InventoryMenuBuilder(int size) {
        this();
        withSize(size);
    }

    public InventoryMenuBuilder(int size, String title) {
        this(size);
        withTitle(title);
    }

    public InventoryMenuBuilder(InventoryType type) {
        this();
        withType(type);
    }

    public InventoryMenuBuilder(InventoryType type, String title) {
        this(type);
        withTitle(title);
    }

    protected void initInventory(Inventory inventory) {
        if (this.inventory != null) {
            throw new IllegalStateException("Inventory already initialized");
        }
        this.inventory = inventory;
    }

    protected void validateInit() {
        if (this.inventory == null) {
            throw new IllegalStateException("inventory not yet initialized");
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public InventoryMenuBuilder withSize(int size) {
        initInventory(Bukkit.createInventory(null, size));
        return this;
    }

    public InventoryMenuBuilder withType(InventoryType type) {
        initInventory(Bukkit.createInventory(null, type));
        return this;
    }

    public InventoryMenuBuilder withTitle(String title) {
        return withTitle(title, true);
    }

    public InventoryMenuBuilder withTitle(String title, boolean refresh) {
        validateInit();
        InventoryHelper.changeTitle(this.inventory, title);
        if (refresh) {
            for (HumanEntity viewer : this.inventory.getViewers()) {
                viewer.closeInventory();
                viewer.openInventory(this.inventory);
            }
        }
        return this;
    }

    public InventoryMenuBuilder withEventHandler(InventoryEventHandler eventHandler) {
        try {
            Skywars.instance.inventoryListener.registerEventHandler(this, eventHandler);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        return this;
    }

    public InventoryMenuBuilder onInteract(InventoryMenuListener listener, ClickType... actions) {
        if ((actions == null) || ((actions != null) && (actions.length == 0))) {
            throw new IllegalArgumentException("must specify at least one action");
        }
        try {
            Skywars.instance.inventoryListener.registerListener(this, listener, actions);
        } catch (IllegalArgumentException e) {
            throw e;
        }
        return this;
    }

    public InventoryMenuBuilder withItem(ItemStack item) {
        validateInit();
        this.inventory.setItem(this.inventory.firstEmpty(), item);
        return this;
    }

    public InventoryMenuBuilder withItem(int slot, ItemStack item) {
        validateInit();
        this.inventory.setItem(slot, item);
        return this;
    }

    public InventoryMenuBuilder withItem(final ItemStack item, final ItemListener listener, ClickType... actions) {
        int firstEmpty = inventory.firstEmpty();
        withItem(firstEmpty, item);
        onInteract(new InventoryMenuListener() {
            public void interact(Player player, ClickType action, int slot_) {
                if (slot_ == firstEmpty) {
                    listener.onInteract(player, action, item);
                }
            }
        }, actions);

        return this;
    }

    public InventoryMenuBuilder withItem(final int slot, final ItemStack item, final ItemListener listener, ClickType... actions) {
        withItem(slot, item);
        onInteract(new InventoryMenuListener() {
            public void interact(Player player, ClickType action, int slot_) {
                if (slot_ == slot) {
                    listener.onInteract(player, action, item);
                }
            }
        }, actions);

        return this;
    }

    public InventoryMenuBuilder withItem(ItemCallback callback) {
        this.callbackItems.add(callback);
        return this;
    }

    public Inventory build() {
        return this.inventory;
    }

    public InventoryMenuBuilder show(HumanEntity... viewers) {
        refreshContent();
        for (HumanEntity viewer : viewers) {
            viewer.openInventory(build());
        }
        return this;
    }

    public InventoryMenuBuilder refreshContent() {
        for (ItemCallback callback : this.callbackItems) {
            int slot = callback.getSlot();
            ItemStack item = callback.getItem();

            withItem(slot, item);
        }
        return this;
    }

    public void dispose() {
        Skywars.instance.inventoryListener.unregisterAllListeners(getInventory());
    }

    public void unregisterListener(InventoryMenuListener listener) {
        try {
            Skywars.instance.inventoryListener.registerListener(this, listener, ALL_CLICK_TYPES);
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }
}
