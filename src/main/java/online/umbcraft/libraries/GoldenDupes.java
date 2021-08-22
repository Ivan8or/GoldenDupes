package online.umbcraft.libraries;

import online.umbcraft.libraries.config.ConfigAutofill;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.dupes.AutocraftDupe;
import online.umbcraft.libraries.dupes.DonkeyDupe;
import online.umbcraft.libraries.dupes.NetherPortalDupe;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;


public final class GoldenDupes extends JavaPlugin {


    public void onEnable() {

        //bStats metrics
        Metrics metrics = new Metrics(this, 11145);

        metrics.addCustomChart(new SimpleBarChart("Server Size",

                () -> {
                    Map<String, Integer> map = new HashMap<>();
                    int playercount = getServer().getOnlinePlayers().size();

                    int[] cats = {0, 0, 5, 10, 20, 50, 75, 100};
                    int index = -1;
                    for (int i = 1; i < cats.length; i++) {
                        String barName = (cats[i-1] + 1) + "-" + (cats[i]);
                        if (playercount > cats[i-1] && playercount <= cats[i]) {
                            map.put(barName, 1);
                            index = i;
                        }
                        else
                            map.put(barName, 0);
                    }

                    String catchAllCat = (cats[cats.length - 1] + 1) + "+";
                    map.put(catchAllCat, (index == -1) ? 1 : 0);
                    return map;

                }));

        // fixing up config if it doesn't have some particular settings
        ConfigAutofill.autofill(this);


        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.AUTOCRAFT_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new AutocraftDupe(this), this);
        }

        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.DONKEY_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new DonkeyDupe(this), this);
        }

        // starts nether portal minecart dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.NETHER_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new NetherPortalDupe(this), this);
        }
    }

    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

}