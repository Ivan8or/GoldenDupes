package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.utils.ConfigPath;
import online.umbcraft.libraries.utils.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class AnvilDupe extends Dupe implements Listener {

    final private Set<Location> anvilsInUse = new HashSet<>();

    public AnvilDupe() {
    }

    // gives the player an extra item stack after their anvil breaks w/ a full inventory
    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilUse(final InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();

        if (inv == null)
            return;
        if (inv.getType() != InventoryType.ANVIL)
            return;
        if(e.getCurrentItem() == null)
            return;

        if(e.getRawSlot() != 2)
            return;

        Location l = e.getClickedInventory().getLocation();
        Location blockBelow = l.clone().add(new Vector(0,-1,0));
        ItemStack toDupe = e.getCurrentItem();
        Player p = (Player) e.getWhoClicked();

        // return if we require player inv to be full and it isn't
        if (GoldenDupes.getInstance().getConfig().getBoolean(ConfigPath.ANVIL_FULLINV.path()) && p.getInventory().firstEmpty() != -1) {
            return;
        }

        // return if anvil is not about to break
        if(!MaterialUtil.isAnvil(l.getBlock()))
            return;

        // preventing the anvil or the block beneath the anvil from being broken / pushed
        anvilsInUse.add(l);
        anvilsInUse.add(blockBelow);

        // check if anvil was destroyed by next tick


        Runnable dropItem = () -> {
            anvilsInUse.remove(l);
            anvilsInUse.remove(blockBelow);

            if (toDupe == null || l.getBlock().getType() != Material.AIR) {
                return;
            }
            ItemStack duped_item = dupe(toDupe, toDupe.getAmount());

            Item dropped = p.getWorld().dropItem(p.getLocation(), duped_item);
            dropped.setVelocity(p.getEyeLocation().getDirection());
        };

        if (GoldenDupes.isFolia()) {
            Bukkit.getRegionScheduler().runDelayed(GoldenDupes.getInstance(), p.getLocation() , t -> dropItem.run(), 1L);
        }
        else {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), dropItem, 1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreakInUseAnvil(final BlockBreakEvent e) {
        if(anvilsInUse.isEmpty())
            return;

        if(anvilsInUse.contains(e.getBlock().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonPush(final BlockPistonExtendEvent e) {
        if(!isPistoningSafe(e.getBlocks()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonPull(final BlockPistonRetractEvent e) {
        if(!isPistoningSafe(e.getBlocks()))
            e.setCancelled(true);
    }

    private boolean isPistoningSafe(List<Block> blocks) {
        if(anvilsInUse.isEmpty())
            return true;

        for(Location anvilBlock : anvilsInUse) {
            boolean involvesAnvil = blocks.stream()
                    .map(Block::getLocation)
                    .anyMatch((l) -> l.equals(anvilBlock));

            if(involvesAnvil)
                return false;
        }
        return true;
    }
}
