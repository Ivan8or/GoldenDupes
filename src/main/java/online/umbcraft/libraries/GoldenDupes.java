package online.umbcraft.libraries;

import online.umbcraft.libraries.config.ConfigAutofill;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.dupes.AutocraftDupe;
import online.umbcraft.libraries.dupes.DonkeyDupe;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class GoldenDupes extends JavaPlugin {


    public void onEnable() {

        // fixing up config if it doesn't have some particular settings
        ConfigAutofill.autofill(this);


        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.AUTOCRAFT_DO.path())) {
            Bukkit.getServer().getPluginManager().registerEvents(
                    new AutocraftDupe(this), this);
        }

        // starts autocraft dupe handler if the dupe is enabled
        if (getConfig().getBoolean(ConfigPath.DONKEY_DO.path())) {
            Bukkit.getServer().getPluginManager().registerEvents(
                    new DonkeyDupe(this), this);
        }

    }

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

}