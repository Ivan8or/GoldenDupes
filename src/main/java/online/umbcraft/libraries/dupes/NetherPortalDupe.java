package online.umbcraft.libraries.dupes;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;
import java.util.*;

public class NetherPortalDupe extends Dupe implements Listener {

    // collection of all items removed by players from minecarts in the recent past
    final private Map<UUID,List<DupedItem>> dupedItems;

    // tracks all minecarts that have recently been transported through a portal
    final private Map<UUID, Integer> transported;

    // tracks all minecarts that have recently been transported trough a portal with Folia ScheudledTask Type
    final private Map<UUID, ScheduledTask> transported_folia;

    public NetherPortalDupe() {
        transported = new HashMap<>();
        transported_folia = new HashMap<>();
        dupedItems = new HashMap<>();
    }

    public void delayCartReuse(UUID minecart) {
        if(transported.containsKey(minecart)) {
            if (GoldenDupes.isFolia) {
                transported_folia.get(minecart).cancel();
            }
            else {
                Bukkit.getServer().getScheduler().cancelTask(transported.get(minecart));
            }
        }
        if (GoldenDupes.isFolia) {
            ScheduledTask newID  = Bukkit.getGlobalRegionScheduler().runDelayed(GoldenDupes.getInstance(), t -> {
                transported_folia.remove(minecart);
            }, 20L);
            transported_folia.put(minecart, newID);
        }
        else {
            int newID = Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), () -> {
                transported.remove(minecart);
            }, 20L);
            transported.put(minecart, newID);
        }


    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMinecartThroughPortal(final EntityPortalEnterEvent e) {

        if(!(e.getEntity() instanceof StorageMinecart))
            return;

        StorageMinecart cart = (StorageMinecart) e.getEntity();
        UUID cartID = cart.getUniqueId();
        if (GoldenDupes.isFolia) {
            if(transported_folia.containsKey(cartID)) {
                delayCartReuse(cartID);
                return;
            }
        }
        else {
            if(transported.containsKey(cartID)) {
                delayCartReuse(cartID);
                return;
            }
        }

        delayCartReuse(cartID);

        List<DupedItem> items = dupedItems.get(cart.getUniqueId());

        if(items == null)
            return;

        for(DupedItem i: items) {
            ItemStack item = i.getItem();
            if(item != null) {
                cart.getInventory().setItem(i.getSlot(), dupe(item, item.getAmount()));
            }
        }
        items.clear();
        dupedItems.remove(cartID);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseMinecartChest(final InventoryClickEvent e) {

        final Inventory inv = e.getClickedInventory();
        if(inv == null)
            return;

        final InventoryHolder holder = inv.getHolder();
        if(!(holder instanceof StorageMinecart))
            return;

        final UUID cartUUID = ((StorageMinecart) holder).getUniqueId();

        final int slot = e.getSlot();
        final int tickSpace = GoldenDupes.getInstance().getConfig().getInt(ConfigPath.NETHER_TICKDELAY.path());
        switch(e.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
            case COLLECT_TO_CURSOR:
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                trackDupedItem(e.getCurrentItem(), slot, cartUUID, tickSpace);
                break;
        }
    }

    private void trackDupedItem(final ItemStack item, final int slot, final UUID uuid, final int ticks) {

        if(item == null || item.getType() == Material.AIR) {
            return;
        }

        final DupedItem cloned = new DupedItem(item, slot);

        if(!dupedItems.containsKey(uuid))
            dupedItems.put(uuid, new ArrayList<>());

        final List<DupedItem> cartItems = dupedItems.get(uuid);
        cartItems.add(cloned);
        if (GoldenDupes.isFolia) {
            Bukkit.getServer().getGlobalRegionScheduler().runDelayed(GoldenDupes.getInstance(), t -> {
                cartItems.remove(cloned);
                if(cartItems.isEmpty())
                    dupedItems.remove(uuid);
            }, ticks);
        }
        else {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(),
                    () -> {
                        cartItems.remove(cloned);
                        if(cartItems.isEmpty())
                            dupedItems.remove(uuid);
                    }, ticks);
        }

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
