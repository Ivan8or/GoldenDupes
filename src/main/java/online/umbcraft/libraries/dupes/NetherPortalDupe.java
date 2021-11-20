package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.schedule.DupeScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

import static online.umbcraft.libraries.config.ConfigPath.*;

public class NetherPortalDupe implements Listener {

    final private GoldenDupes plugin;

    // collection of all items removed by players from minecarts in the recent past
    final private Map<UUID,List<DupedItem>> dupedItems;

    // tracks all minecarts that have recently been transported through a portal
    final private Map<UUID, Integer> transported;

    final private DupeScheduler portalScheduler;

    public NetherPortalDupe(final GoldenDupes plugin) throws IOException {
        this.plugin = plugin;

        portalScheduler = new DupeScheduler(
                plugin,
                "portal",
                plugin.getConfig().getInt(NETHER_ON.path()),
                plugin.getConfig().getInt(NETHER_OFF.path())
        );


        transported = new HashMap<>();
        dupedItems = new HashMap<>();
    }

    public DupeScheduler getScheduler() {
        return portalScheduler;
    }

    public void delayCartReuse(UUID minecart) {
        if(transported.containsKey(minecart))
            plugin.getServer().getScheduler().cancelTask(transported.get(minecart));

        int newID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            transported.remove(minecart);
        }, 20L);

        transported.put(minecart, newID);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMinecartThroughPortal(final EntityPortalEnterEvent e) {

        if(!portalScheduler.isEnabled())
            return;

        if(!(e.getEntity() instanceof StorageMinecart))
            return;

        StorageMinecart cart = (StorageMinecart) e.getEntity();
        UUID cartID = cart.getUniqueId();

        if(transported.containsKey(cartID)) {
            delayCartReuse(cartID);
            return;
        }
        delayCartReuse(cartID);

        List<DupedItem> items = dupedItems.get(cart.getUniqueId());

        if(items == null)
            return;

        for(DupedItem i: items) {
            ItemStack item = i.getItem();

            if (
                    item.getMaxStackSize() == 1 &&
                            !plugin.getConfig().getBoolean(NON_STACK_DO_DUPE.path())
            ) continue;

            if (
                    item.getType().name().contains("SHULKER_BOX") &&
                            !plugin.getConfig().getBoolean(SHULKERS_DO_DUPE.path())
            ) continue;

            if (
                    item.getType() == Material.TOTEM_OF_UNDYING &&
                            !plugin.getConfig().getBoolean(TOTEMS_DO_DUPE.path())
            ) continue;

            cart.getInventory().setItem(i.getSlot(), item);
        }
        items.clear();
        dupedItems.remove(cartID);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseMinecartChest(final InventoryClickEvent e) {

        if(!portalScheduler.isEnabled())
            return;

        final Inventory inv = e.getClickedInventory();
        if(inv == null)
            return;

        final InventoryHolder holder = inv.getHolder();
        if(!(holder instanceof StorageMinecart))
            return;

        final UUID cartUUID = ((StorageMinecart) holder).getUniqueId();

        final int slot = e.getSlot();
        final int tickSpace = plugin.getConfig().getInt(ConfigPath.NETHER_TICKDELAY.path());
        switch(e.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
                trackDupedItem(e.getCurrentItem(), slot, cartUUID, tickSpace);
                break;
            case COLLECT_TO_CURSOR:
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                trackDupedItem(e.getCurrentItem(), slot, cartUUID, tickSpace);
        }
    }

    private void trackDupedItem(final ItemStack item, final int slot, final UUID uuid, final int ticks) {

        if(item.getType() == Material.AIR) {
            return;
        }

        final DupedItem cloned = new DupedItem(item, slot);

        if(!dupedItems.containsKey(uuid))
            dupedItems.put(uuid, new ArrayList<>());

        final List<DupedItem> cartItems = dupedItems.get(uuid);
        cartItems.add(cloned);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                () -> {
                    cartItems.remove(cloned);
                    if(cartItems.isEmpty())
                        dupedItems.remove(uuid);
                }, ticks);
    }

    private class DupedItem {
        final private ItemStack item;
        final private int slot;

        public DupedItem(final ItemStack item, final int slot) {
            this.item = item.clone();
            this.slot = slot;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        public int getSlot() {
            return slot;
        }
    }
}
