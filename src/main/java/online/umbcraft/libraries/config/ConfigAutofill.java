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

        for(ConfigPath setting: ConfigPath.values()) {
            if(!config.contains(setting.path())) {
                config.set(setting.path(), setting.value());
            }
        }
    }
}
