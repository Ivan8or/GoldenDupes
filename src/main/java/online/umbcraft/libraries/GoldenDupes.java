package online.umbcraft.libraries;

import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.dupes.AutocraftDupe;
import online.umbcraft.libraries.dupes.DonkeyDupe;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;


public final class GoldenDupes extends JavaPlugin {


    public void onEnable() {

        // creating default config if it does not exist
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists())
            this.saveDefaultConfig();


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