package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static online.umbcraft.libraries.config.ConfigPath.*;

public class AnvilDupe extends Dupe implements Listener {

    final private Set<Location> anvilsInUse = new HashSet<>();

    public AnvilDupe(GoldenDupes plugin) {
        super(plugin);
    }

    // gives the player an extra item stack after their anvil breaks w/ a full inventory
    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilUse(final InventoryClickEvent e) {
        Inventory t = e.getClickedInventory();

        if (t == null)
            return;
        if (t.getType() != InventoryType.ANVIL)
            return;
        if(e.getCurrentItem() == null)
            return;

        if(e.getRawSlot() != 2)
            return;

        Location l = e.getClickedInventory().getLocation();
        ItemStack toDupe = e.getCurrentItem();
        Player p = (Player) e.getWhoClicked();

        // return if player has free inventory slot
        if(p.getInventory().firstEmpty() != -1)
            return;

        // return if anvil is not about to break
        if(l.getBlock().getType() != Material.DAMAGED_ANVIL)
            return;

        anvilsInUse.add(l);

        // check if anvil was destroyed by next tick
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
        {
            anvilsInUse.remove(l);

            if (l.getBlock().getType() == Material.AIR && toDupe != null) {
                Item dropped = p.getWorld().dropItem(p.getLocation(), dupe(toDupe, toDupe.getAmount()));
                dropped.setVelocity(p.getEyeLocation().getDirection());
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakInUseAnvil(final BlockBreakEvent e) {
        if(e.getBlock().getType() != Material.DAMAGED_ANVIL)
            return;

        if(anvilsInUse.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }
}
