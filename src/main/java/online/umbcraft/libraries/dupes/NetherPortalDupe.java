package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class NetherPortalDupe implements Listener {

    final private GoldenDupes plugin;
    final private Map<UUID,List<DupedItem>> dupedItems;
    final private int tickSpace = 50;

    public NetherPortalDupe(final GoldenDupes plugin) {
        this.plugin = plugin;
        dupedItems = new HashMap<>();
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onMinecartThroughPortal(final EntityPortalEnterEvent e) {

        if(!(e.getEntity() instanceof StorageMinecart))
            return;

        Bukkit.broadcastMessage("minecart went through portal");
        StorageMinecart cart = (StorageMinecart) e.getEntity();
        cart.teleport(cart.getLocation().add(4,0,0));
        List<DupedItem> items = dupedItems.get(cart.getUniqueId());

        if(items == null)
            return;

        for(DupedItem i: items) {
            Bukkit.broadcastMessage("cloning "+i.getItem().getType()+" back to cart!");
            cart.getInventory().setItem(i.getSlot(), i.getItem());
        }
        items.clear();

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseMinecartChest(final InventoryClickEvent e) {

        final InventoryHolder holder = e.getClickedInventory().getHolder();
        if(!(holder instanceof StorageMinecart))
            return;

        final UUID cartUUID = ((StorageMinecart) holder).getUniqueId();


        final int slot = e.getSlot();
        switch(e.getAction()) {
            case MOVE_TO_OTHER_INVENTORY:
                trackDupedItem(e.getCurrentItem(), slot, cartUUID, tickSpace);
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
            Bukkit.broadcastMessage("material is AIR >:(");
            return;
        }

        final DupedItem cloned = new DupedItem(item, slot);

        if(!dupedItems.containsKey(uuid))
            dupedItems.put(uuid, new ArrayList<>());

        final List<DupedItem> cartItems = dupedItems.get(uuid);
        cartItems.add(cloned);
        Bukkit.broadcastMessage("minecart item is ready to be duped");
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                () -> {
                    cartItems.remove(cloned);
                    Bukkit.broadcastMessage("minecart item is no longer dupeable");
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
