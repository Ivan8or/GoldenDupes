package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AnvilDupe implements Listener {

    // super speedy set of all shulker items
    final private EnumSet<Material> shulkerBoxes;

    final private GoldenDupes plugin;

    public AnvilDupe(final GoldenDupes plugin) {
        this.plugin = plugin;

        // building an EnumSet of all shulkers
        shulkerBoxes = EnumSet.noneOf(Material.class);
        Arrays.stream(Material.values())
                .filter(m -> m.toString().endsWith("SHULKER_BOX"))
                .forEach(shulkerBoxes::add);
    }

    // gives the player the extra items once they pick up after performing the dupe
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


        // check if anvil was destroyed by next tick
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () ->
        {
            if (l.getBlock().getType() == Material.AIR) {
                Item dropped = p.getWorld().dropItem(p.getLocation(),toDupe.clone());
                dropped.setVelocity(p.getEyeLocation().getDirection());
            }
        }, 1L);

    }
}
