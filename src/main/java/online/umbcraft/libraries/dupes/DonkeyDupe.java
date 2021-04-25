package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
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


    // detects for players viewing the donkey's inventory whenever a player dc's riding a donkey
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent e) {

        Entity vehicle = e.getPlayer().getVehicle();

        if(plugin.getConfig().getBoolean(ConfigPath.DONKEY_BOATS.name()))
            traverseBoat(vehicle);
        else
            dupeInventory(vehicle);
    }

    // dupes inventories of all dokeys/llamas/mules in a boat
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

    // dupes the inventory of a donkey/llama/mule for all players viewing it
    private void dupeInventory(Entity riding) {

        if (!(riding instanceof AbstractHorse))
            return;

        AbstractHorse donkey = (AbstractHorse) riding;
        Inventory cloned = clone(donkey);
        List<HumanEntity> viewers = donkey.getInventory().getViewers();

        // weird iteration because iterators gave too much trouble
        for (int i = viewers.size() - 1; i >= 0; i--) {
            HumanEntity human = viewers.get(i);
            human.closeInventory();
            human.openInventory(cloned);
        }
    }

    // creates a copy of a donkey/llama/mule inventory
    private Inventory clone(AbstractHorse donkey) {

        Inventory toClone = donkey.getInventory();
        Inventory result = Bukkit.createInventory(null, toClone.getType());

        for (int i = 0; i <= 16; i++) {
            result.setItem(i, toClone.getItem(i));
        }
        return result;
    }
}
