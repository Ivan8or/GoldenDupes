package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.utils.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static online.umbcraft.libraries.utils.ConfigPath.*;

public abstract class Dupe {

    private static boolean debugLogs;

    private static boolean dupeNonStacking;
    private static int nonStackingStackSize;

    private static boolean dupeShulkers;
    private static int shulkerStackSize;

    private static boolean dupeTotems;
    private static int totemStackSize;

    private static List<Map> dupeRules;

    public Dupe() {
    }

    // loads cofig values regarding item limits
    public static void loadConfig(FileConfiguration config) {
        dupeNonStacking = config.getBoolean(NON_STACK_DO_DUPE.path());
        nonStackingStackSize = config.getInt(NON_STACK_STACKSIZE.path());

        dupeShulkers = config.getBoolean(SHULKERS_DO_DUPE.path());
        shulkerStackSize = config.getInt(SHULKERS_STACKSIZE.path());

        dupeTotems = config.getBoolean(TOTEMS_DO_DUPE.path());
        totemStackSize = config.getInt(TOTEMS_STACKSIZE.path());

        dupeRules = (List<Map>) config.getList(DUPE_RULES.path());
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

        if(!dupeRules.isEmpty()) {
            Map match = dupeRules.stream()
                .filter(e -> toDupe.getType().toString().matches((String) e.getOrDefault("match", "THIS_MATERIAL_DOES_NOT_EXIST_AAAAAAAAH")))
                .findFirst().orElse(null);
            if(match != null){
                int matchStackTo = (Integer) match.getOrDefault("stack-to", toDupe.getMaxStackSize());
                if(matchStackTo <= 0) {
                    matchStackTo = toDupe.getMaxStackSize();
                }
                boolean matchDupe = (Boolean) match.getOrDefault("dupe", false);
                return matchDupe ? Math.min(idealAmount, matchStackTo) : 0;
            }
        }

        // legacy controls

        if (!isSize64) {
            dupe = dupeNonStacking;
            stacksize = Math.min(idealAmount, nonStackingStackSize);
        }

        if (MaterialUtil.isTotem(toDupe.getType())) {
            dupe = dupeTotems;
            stacksize = Math.min(idealAmount, totemStackSize);
        }

        if (MaterialUtil.isShulkerBox(toDupe)) {
            dupe = dupeShulkers;
            stacksize = Math.min(idealAmount, shulkerStackSize);
        }

        if (!dupe)
            return 0;

        return stacksize;
    }
}
