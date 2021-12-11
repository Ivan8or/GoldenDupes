package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.inventory.ItemStack;

import static online.umbcraft.libraries.config.ConfigPath.NON_STACK_DO_DUPE;

public abstract class Dupe {

    final protected GoldenDupes plugin;

    public Dupe(GoldenDupes plugin) {
        this.plugin = plugin;
    }


    public ItemStack dupeItem(ItemStack toDupe) {
        plugin.getConfig().getBoolean(NON_STACK_DO_DUPE.path());
        return null;
    }
}
