package online.umbcraft.libraries.utils;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Scheduler {

    public void in(long ticks, Location l, Runnable event) {
        if (GoldenDupes.isFolia()) {
            Bukkit.getRegionScheduler().runDelayed(GoldenDupes.getInstance(), l , t -> event.run(), ticks);
        }
        else {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), event, ticks);
        }
    }

}
