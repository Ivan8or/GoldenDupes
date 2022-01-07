package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.EnumSet;

import static online.umbcraft.libraries.config.ConfigPath.*;

public abstract class Dupe {

    final protected GoldenDupes plugin;

    final private boolean TOTEMS_EXIST;
    final private boolean SHULKERS_EXIST;

    final private EnumSet<Material> shulkerBoxes;

    private boolean dupeNonStacking;
    private int nonStackingStackSize;

    private boolean dupeShulkers;
    private int shulkerStackSize;

    private boolean dupeTotems;
    private int totemStackSize;

    public Dupe(GoldenDupes plugin) {
        this.plugin = plugin;
        loadConfig(plugin.getConfig());

        TOTEMS_EXIST = Material.matchMaterial("minecraft:totem_of_undying") != null;
        SHULKERS_EXIST = Material.matchMaterial("minecraft:shulker_shell") != null;

        shulkerBoxes = EnumSet.noneOf(Material.class);


        // building an EnumSet of all colors of shulker box
        if (SHULKERS_EXIST)
            Arrays.stream(Material.values())
                    .filter(m -> m.name().endsWith("SHULKER_BOX"))
                    .forEach(shulkerBoxes::add);
    }

    public void loadConfig(FileConfiguration config) {
        dupeNonStacking = config.getBoolean(NON_STACK_DO_DUPE.path());
        nonStackingStackSize = config.getInt(NON_STACK_STACKSIZE.path());

        dupeShulkers = config.getBoolean(SHULKERS_DO_DUPE.path());
        shulkerStackSize = config.getInt(SHULKERS_STACKSIZE.path());

        dupeTotems = config.getBoolean(TOTEMS_DO_DUPE.path());
        totemStackSize = config.getInt(TOTEMS_STACKSIZE.path());
    }


    public ItemStack dupe(ItemStack toDupe, int amount) {

        if (toDupe == null)
            return new ItemStack(Material.AIR);

        boolean dupe = false;
        int stacksize = amount;
        boolean isSize64 = toDupe.getMaxStackSize() == 64;


        if (!isSize64) {
            dupe = dupeNonStacking;
            stacksize = Math.max(stacksize, nonStackingStackSize);
        }

        if (TOTEMS_EXIST && toDupe.getType() == Material.TOTEM_OF_UNDYING) {
            dupe = dupeTotems;
            stacksize = Math.max(stacksize, totemStackSize);
        }

        if (SHULKERS_EXIST && shulkerBoxes.contains(toDupe.getType())) {
            dupe = dupeShulkers;
            stacksize = Math.max(stacksize, shulkerStackSize);
        }

        if (!dupe)
            return new ItemStack(Material.AIR);

        ItemStack duped = toDupe.clone();
        duped.setAmount(stacksize);
        return duped;
    }
}
