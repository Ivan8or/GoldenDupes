package online.umbcraft.libraries;

import online.umbcraft.libraries.config.ConfigAutofill;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.dupes.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;


public final class GoldenDupes extends JavaPlugin {

    private static GoldenDupes instance;

    public void onReloadCommand() {
        reloadConfig();
    }

    public void onEnable() {
        instance = this;

        //bStats metrics
        Metrics metrics = new Metrics(this, 11145);

        // currently does not exist - placeholder
        metrics.addCustomChart(new SimpleBarChart("server_sizes_bar",
                () -> {
                    Map<String, Integer> map = new HashMap<>();
                    int playercount = getServer().getOnlinePlayers().size();

                    String[] categories = {"0","1","2-4","5-10","11-20","21-30","31-50","51-75","76-100","101+"};
                    int[] limits =         {0,1,2,5,11,21,31,51,76,100};

                    for(String cat: categories)
                        map.put(cat,0);

                    for(int i = 0; i < categories.length; i++)
                        if(playercount <= limits[i]) {
                            map.put(categories[i], 1);
                            return map;
                        }

                    map.put(categories[categories.length-1],1);
                    return map;
                }));


        metrics.addCustomChart(new SimplePie("server_sizes_pie",
                () -> {
                    int playercount = getServer().getOnlinePlayers().size();

                    String[] categories = {"0","1","2-4","5-10","11-20","21-30","31-50","51-75","76-100","101+"};
                    int[] limits =         {0,1,2,5,11,21,31,51,76,100};

                    for(int i = 0; i < categories.length; i++)
                        if(playercount <= limits[i]) {
                            return categories[i];
                        }
                    return categories[categories.length-1];
                }));

        final ConfigPath[] allDoFlags = {
                ConfigPath.AUTOCRAFT_DO,
                ConfigPath.PISTON_DO,
                ConfigPath.NETHER_DO,
                ConfigPath.ANVIL_DO,
                ConfigPath.DONKEY_DO};


        for(ConfigPath dupe: allDoFlags) {
            metrics.addCustomChart(new SimplePie(dupe.toString(),
                    () -> getConfig().getString(dupe.path())));
        }


        // fixing up config if it doesn't have some particular settings
        ConfigAutofill.autofill(this);

        Dupe.loadConfig(getConfig());

        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.AUTOCRAFT_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new AutocraftDupe(), this);
        }

        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.DONKEY_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new DonkeyDupe(), this);
        }

        // starts nether portal minecart dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.NETHER_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new NetherPortalDupe(), this);
        }

        // starts anvil dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.ANVIL_DO.path())) {
        getServer().getPluginManager().registerEvents(
                new AnvilDupe(), this);
        }

        // starts piston dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.PISTON_DO.path())) {
            getServer().getPluginManager().registerEvents(
                    new PistonDupe(), this);
        }

    }

    public int serverVersion() {
        return Integer.parseInt(getServer().getBukkitVersion()
                .replaceFirst("^(\\d+)\\.", "")
                .replaceAll("\\.(.+)", "")
        );
    }

    public static GoldenDupes getInstance() {
        return instance;
    }

}