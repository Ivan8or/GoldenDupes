package online.umbcraft.libraries.dupes;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;


public class DonkeyDupe implements Listener {

    GoldenDupes plugin;

    public DonkeyDupe(GoldenDupes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent e) {

        Entity vehicle = e.getPlayer().getVehicle();
        dupeInventoryR(vehicle);
    }

    private void dupeInventoryR(Entity riding) {

        Boat boat = null;

        if (riding instanceof Boat)
            boat = (Boat) riding;

        if (riding.getVehicle() instanceof Boat)
            boat = (Boat) riding.getVehicle();

        if (boat != null)
            for (Entity passenger : boat.getPassengers()) {
                dupeInventoryR(passenger);
            }

        if (!(riding instanceof AbstractHorse))
            return;

        AbstractHorse donkey = (AbstractHorse) riding;
        Inventory cloned = clone(donkey);
        List<HumanEntity> viewers = donkey.getInventory().getViewers();
        for (int i = viewers.size() - 1; i >= 0; i--) {
            HumanEntity human = viewers.get(i);
            System.out.println("opening for " + human.getName());
            human.closeInventory();
            human.openInventory(cloned);
        }
    }

    private Inventory clone(AbstractHorse donkey) {

        Inventory toClone = donkey.getInventory();
        Inventory result = Bukkit.createInventory(null, toClone.getType());

        for (int i = 0; i <= 16; i++) {
            result.setItem(i, toClone.getItem(i));
        }
        return result;
    }
}
