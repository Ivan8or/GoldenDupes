package online.umbcraft.libraries.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class ConfigAutofill {

    public static void autofill(Plugin plugin) {

        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
            return;
        }

        FileConfiguration config = plugin.getConfig();
        int updates = 0;
        for(ConfigPath setting: ConfigPath.values()) {
            System.out.println("checking setting - "+setting.path());
            if(!config.contains(setting.path())) {
                System.out.println("\tdoesn't exist: ADDING!");
                updates++;
                config.set(setting.path(), setting.value());
            }
            else
                System.out.println("\texists");
        }
        if(updates > 0)
            plugin.saveConfig();
    }
}
