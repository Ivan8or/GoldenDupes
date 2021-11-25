package online.umbcraft.libraries;

import online.umbcraft.libraries.config.ConfigAutofill;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.dupes.AnvilDupe;
import online.umbcraft.libraries.dupes.AutocraftDupe;
import online.umbcraft.libraries.dupes.DonkeyDupe;
import online.umbcraft.libraries.dupes.NetherPortalDupe;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimpleBarChart;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public final class GoldenDupes extends JavaPlugin {

    private AnvilDupe anvilDupe;
    private AutocraftDupe autocraftDupe;
    private DonkeyDupe donkeyDupe;
    private NetherPortalDupe netherPortalDupe;

    public AnvilDupe getAnvilDupe() {
        return anvilDupe;
    }
    public AutocraftDupe getAutocraftDupe() {
        return autocraftDupe;
    }
    public DonkeyDupe getDonkeyDupe() {
        return donkeyDupe;
    }
    public NetherPortalDupe getNetherPortalDupe() {
        return netherPortalDupe;
    }

    public void onEnable() {

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


        // papi integration
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            DupePlaceholder phd = new DupePlaceholder(this);
            phd.register();
        }


        // fixing up config if it doesn't have some particular settings
        ConfigAutofill.autofill(this);

        try {

            // starts autocraft dupe handler if the dupe is enabled
            if (getConfig().getBoolean(ConfigPath.AUTOCRAFT_DO.path())) {
                autocraftDupe = new AutocraftDupe(this);
                getServer().getPluginManager().registerEvents(
                        autocraftDupe, this);
            }

            // starts autocraft dupe handler if the dupe is enabled
            if (getConfig().getBoolean(ConfigPath.DONKEY_DO.path())) {
                donkeyDupe = new DonkeyDupe(this);
                getServer().getPluginManager().registerEvents(
                        donkeyDupe, this);
            }

            // starts nether portal minecart dupe handler if the dupe is enabled
            if (getConfig().getBoolean(ConfigPath.NETHER_DO.path())) {
                netherPortalDupe = new NetherPortalDupe(this);
                getServer().getPluginManager().registerEvents(
                        netherPortalDupe, this);
            }

            if (getConfig().getBoolean(ConfigPath.ANVIL_DO.path())) {
                anvilDupe = new AnvilDupe(this);
                getServer().getPluginManager().registerEvents(
                        anvilDupe, this);
            }

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}