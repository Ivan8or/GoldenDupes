package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class PistonDupe extends Dupe implements Listener {

    // set of all item frames which qualify to have their items duped
    final private Set<UUID> dupableFrames = new HashSet<>();

    public PistonDupe() {
    }

    // marks an item frame as dupable if it was recently exposed to the back of a retracting piston
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        World world = e.getBlock().getWorld();
        Vector pistonLoc = e.getBlock().getLocation().toVector();

        int xoff = -1 * e.getDirection().getModX();
        int yoff = -1 * e.getDirection().getModY();
        int zoff = -1 * e.getDirection().getModZ();

        // amount to offset the regular piston coords by
        Vector buttOffset = new Vector(0.5*xoff + 0.5,0.5*yoff + 0.5,0.5*zoff + 0.5);

        // center of the face of the butt of the piston
        Location pistonButtInWorld = pistonLoc.add(buttOffset).toLocation(world);

        // all entities pressed against the butt of the piston
        Collection<Entity> nearbyEntities = e.getBlock().getWorld().getNearbyEntities(pistonButtInWorld,0.05,0.05,0.05);

        // mark the itemframe as valid for duping for the next few ticks, specified by tickdelay in the config
        for(Entity frame : nearbyEntities) {
            if (!(frame instanceof ItemFrame))
                continue;

            dupableFrames.add(frame.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), () -> {
                dupableFrames.remove(frame.getUniqueId());
            }, GoldenDupes.getInstance().getConfig().getLong(ConfigPath.PISTON_TICKDELAY.path()));
            return;
        }
    }


    // dupes the item in an item frame as it is being broken (if it was marked as dupable)
    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakFrame(EntityDamageByEntityEvent e) {

        // only continue if an itemframe is damaged
        if(!(e.getEntity() instanceof ItemFrame))
            return;

        ItemFrame frame = (ItemFrame) e.getEntity();

        // ignore frames that are not being duped from
        if(!dupableFrames.contains(frame.getUniqueId()))
            return;

        // only players can trigger the dupe if non-players is set to false
        if(!GoldenDupes.getInstance().getConfig().getBoolean(ConfigPath.PISTON_NONPLAYER.path()) && !(e.getDamager() instanceof Player))
            return;

        // dupe the item and spawn it along with the original item
        ItemStack duped = this.dupe(frame.getItem(),1);
        frame.getWorld().dropItemNaturally(frame.getLocation(), duped);
    }
}
