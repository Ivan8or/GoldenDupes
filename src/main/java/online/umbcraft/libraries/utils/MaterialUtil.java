package online.umbcraft.libraries.utils;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class MaterialUtil {

    public static boolean isTotem(Material m) {
        if (GoldenDupes.getInstance().serverVersion() < 11) {
            return false;
        } else if (GoldenDupes.getInstance().serverVersion() <= 12) {
            return m == Material.valueOf("TOTEM");
        }
        return m == Material.TOTEM_OF_UNDYING;
    }

    public static boolean isAnvil(Block b) {
        if (GoldenDupes.getInstance().serverVersion() <= 12) {
            return b.getType() == Material.valueOf("ANVIL") && b.getData() == 10; // block data of Damaged Anvil is 10 in 1.12.2
        }
        return b.getType() == Material.DAMAGED_ANVIL;
    }

    public static boolean isShulkerBox(ItemStack i) {
        if (GoldenDupes.getInstance().serverVersion() < 11) {
            return false;
        }
        return i.getType().name().contains("SHULKER_BOX");
    }
}
