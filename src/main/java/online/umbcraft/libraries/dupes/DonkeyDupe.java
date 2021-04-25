package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class DonkeyDupe  implements Listener {

    GoldenDupes plugin;

    public DonkeyDupe(GoldenDupes plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerLeave(PlayerQuitEvent e) {

        Entity vehicle = e.getPlayer().getVehicle();
        if(vehicle == null || !(vehicle instanceof Donkey))
            return;

        Donkey donkey = (Donkey) vehicle;

        for(HumanEntity human: donkey.getInventory().getViewers()) {
            human.closeInventory();
            human.openInventory(cloneInventory(donkey.getInventory()));
        }
    }

    private Inventory cloneInventory(Inventory toClone) {
        Inventory result = Bukkit.createInventory(null, toClone.getType(), "DUPED INVENTORY");

        result.setItem(0, toClone.getItem(0));
        for(int i = 2; i <= 16; i++) {
            result.setItem(i, toClone.getItem(i));
        }
        return result;
    }
}
