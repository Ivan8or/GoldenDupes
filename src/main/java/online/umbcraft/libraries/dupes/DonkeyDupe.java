package online.umbcraft.libraries.dupes;

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
        traverseBoat(vehicle);
    }


    private void traverseBoat(Entity riding) {
        Boat boat = null;

        if (riding instanceof Boat)
            boat = (Boat) riding;

        if (riding.getVehicle() instanceof Boat)
            boat = (Boat) riding.getVehicle();

        if (boat != null) {
            for (Entity passenger : boat.getPassengers()) {
                dupeInventory(passenger);
            }
            return;
        }
        dupeInventory(riding);
    }

    private void dupeInventory(Entity riding) {

        if (!(riding instanceof AbstractHorse))
            return;

        AbstractHorse donkey = (AbstractHorse) riding;
        Inventory cloned = clone(donkey);
        List<HumanEntity> viewers = donkey.getInventory().getViewers();
        for (int i = viewers.size() - 1; i >= 0; i--) {
            HumanEntity human = viewers.get(i);
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
