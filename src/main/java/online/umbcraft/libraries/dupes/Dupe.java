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

    final static private boolean TOTEMS_EXIST;
    final static private boolean USE_LEGACY_TOTEM;
    final static private Material TOTEM_MATERIAL;
    final static private boolean SHULKERS_EXIST;

    final static private EnumSet<Material> shulkerBoxes;

    private static boolean dupeNonStacking;
    private static int nonStackingStackSize;

    private static boolean dupeShulkers;
    private static int shulkerStackSize;

    private static boolean dupeTotems;
    private static int totemStackSize;

    static {

        boolean shulkersExist;
        try {
            Material.valueOf("SHULKER_SHELL");
            shulkersExist = true;
        }
        catch(IllegalArgumentException e) {
            shulkersExist = false;
        }

        boolean totemsExist;
        Material totemMaterial = null;
        try {
            totemMaterial = Material.valueOf("TOTEM_OF_UNDYING");
            totemsExist = true;
        }
        catch(IllegalArgumentException e) {
            totemsExist = false;
        }

        boolean legacyTotems = false;

        if(!totemsExist) {
            try {
                totemMaterial = Material.valueOf("TOTEM");
                legacyTotems = true;
                totemsExist = true;
            }
            catch(IllegalArgumentException e) {
                legacyTotems = false;
            }
        }

        TOTEMS_EXIST = totemsExist;
        TOTEM_MATERIAL = totemMaterial;

        SHULKERS_EXIST = shulkersExist;
        USE_LEGACY_TOTEM = legacyTotems;

        shulkerBoxes = EnumSet.noneOf(Material.class);

        System.out.println("do shulkers exist? "+SHULKERS_EXIST);
        System.out.println("do totems exist? "+TOTEMS_EXIST);
        System.out.println("legacy totems? "+USE_LEGACY_TOTEM);
        // building an EnumSet of all colors of shulker box
        if (SHULKERS_EXIST) {
            Arrays.stream(Material.values())
                    .filter(m -> m.name().contains("SHULKER_BOX"))
                    .forEach(shulkerBoxes::add);
        }
    }


    public Dupe(GoldenDupes plugin) {
        this.plugin = plugin;

    }

    // loads cofig values regarding item limits
    public static void loadConfig(FileConfiguration config) {
        dupeNonStacking = config.getBoolean(NON_STACK_DO_DUPE.path());
        nonStackingStackSize = config.getInt(NON_STACK_STACKSIZE.path());

        dupeShulkers = config.getBoolean(SHULKERS_DO_DUPE.path());
        shulkerStackSize = config.getInt(SHULKERS_STACKSIZE.path());

        dupeTotems = config.getBoolean(TOTEMS_DO_DUPE.path());
        totemStackSize = config.getInt(TOTEMS_STACKSIZE.path());
    }



    // dupes an item
    // takes in the item to dupe and the maximum acceptable stack size before considering config limits
    public ItemStack dupe(ItemStack toDupe, int amount) {

        int dupeAmount = newAmount(toDupe, amount);
        if(dupeAmount == 0)
            return new ItemStack(Material.AIR);

        ItemStack duped = toDupe.clone();
        duped.setAmount(dupeAmount);
        return duped;
    }


    public int newAmount(ItemStack toDupe, int idealAmount) {
        if (toDupe == null)
            return 0;

        boolean dupe = true;
        int stacksize = idealAmount;
        boolean isSize64 = toDupe.getMaxStackSize() == 64;

        if (!isSize64) {
            dupe = dupeNonStacking;
            stacksize = Math.min(idealAmount, nonStackingStackSize);
        }

        if (TOTEMS_EXIST && toDupe.getType() == TOTEM_MATERIAL) {
            dupe = dupeTotems;
            stacksize = Math.min(idealAmount, totemStackSize);
        }

        if (SHULKERS_EXIST && shulkerBoxes.contains(toDupe.getType())) {
            dupe = dupeShulkers;
            stacksize = Math.min(idealAmount, shulkerStackSize);
        }

        if (!dupe)
            return 0;

        return stacksize;
    }
}
