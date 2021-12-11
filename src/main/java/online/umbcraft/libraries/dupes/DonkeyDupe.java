package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static online.umbcraft.libraries.config.ConfigPath.*;


public class DonkeyDupe extends Dupe implements Listener {

    public DonkeyDupe(final GoldenDupes plugin) {
        super(plugin);
    }


    // detects for players viewing the donkey's inventory whenever a player dc's riding a donkey
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(final PlayerQuitEvent e) {

        final Entity vehicle = e.getPlayer().getVehicle();

        if(vehicle == null)
            return;

        if (plugin.getConfig().getBoolean(ConfigPath.DONKEY_BOATS.path()))
            traverseBoat(vehicle);
        else
            dupeInventory(vehicle);
    }

    // dupes inventories of all dokeys/llamas/mules in a boat
    private void traverseBoat(final Entity riding) {
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
    private void dupeInventory(final Entity riding) {

        if (!(riding instanceof AbstractHorse))
            return;

        final AbstractHorse donkey = (AbstractHorse) riding;
        final Inventory cloned = clone(donkey);
        final List<HumanEntity> viewers = donkey.getInventory().getViewers();

        // weird iteration because iterators gave too much trouble
        for (int i = viewers.size() - 1; i >= 0; i--) {
            final HumanEntity human = viewers.get(i);
            human.closeInventory();
            human.openInventory(cloned);
        }
    }

    // creates a copy of a donkey/llama/mule inventory
    private Inventory clone(final AbstractHorse donkey) {

        final Inventory toClone = donkey.getInventory();
        final Inventory result = Bukkit.createInventory(null, toClone.getType());

        for (int i = 0; i <= 16; i++) {
            ItemStack item = toClone.getItem(i);

            if (
                    item == null
            ) continue;

            if (
                    item.getMaxStackSize() == 1 &&
                            !plugin.getConfig().getBoolean(NON_STACK_DO_DUPE.path())
            ) break;

            if (
                    item.getType().name().contains("SHULKER_BOX") &&
                            !plugin.getConfig().getBoolean(SHULKERS_DO_DUPE.path())
            ) break;

            if (
                    item.getType() == Material.TOTEM_OF_UNDYING &&
                            !plugin.getConfig().getBoolean(TOTEMS_DO_DUPE.path())
            ) break;

            result.setItem(i, toClone.getItem(i));
        }
        return result;
    }
}
