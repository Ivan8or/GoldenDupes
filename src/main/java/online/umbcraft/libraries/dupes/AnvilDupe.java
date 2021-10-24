package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.schedule.DupeScheduler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

import static online.umbcraft.libraries.config.ConfigPath.*;

public class AnvilDupe implements Listener {

    final private GoldenDupes plugin;
    final private DupeScheduler anvilSchedule;

    public AnvilDupe(final GoldenDupes plugin) throws IOException {
        this.plugin = plugin;
        anvilSchedule = new DupeScheduler(
                plugin,
                "anvil",
                plugin.getConfig().getInt(ANVIL_ON.path()),
                plugin.getConfig().getInt(ANVIL_OFF.path())
        );
    }

    public DupeScheduler getScheduler() {
        return anvilSchedule;
    }

    // gives the player an extra item stack after their anvil breaks w/ a full inventory
    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilUse(final InventoryClickEvent e) {

        if(!anvilSchedule.isEnabled())
            return;

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


        // check if anvil was destroyed by next tick
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
        {

            if (l.getBlock().getType() == Material.AIR) {

                if (
                        toDupe.getMaxStackSize() == 1 &&
                                !plugin.getConfig().getBoolean(NON_STACK_DO_DUPE.path())
                ) return;

                if (
                        toDupe.getType().name().contains("SHULKER_BOX") &&
                                !plugin.getConfig().getBoolean(SHULKERS_DO_DUPE.path())
                ) return;

                if (
                        toDupe.getType() == Material.TOTEM_OF_UNDYING &&
                                !plugin.getConfig().getBoolean(TOTEMS_DO_DUPE.path())
                ) return;

                Item dropped = p.getWorld().dropItem(p.getLocation(),toDupe.clone());
                dropped.setVelocity(p.getEyeLocation().getDirection());
            }

        }, 1L);
    }
}
