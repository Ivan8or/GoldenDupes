package online.umbcraft.libraries.utils;

import online.umbcraft.libraries.GoldenDupes;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialUtil {

    public static Material TOTEM_MATERIAL = (GoldenDupes.getInstance().serverVersion() <= 12) ? Material.valueOf("TOTEM") : Material.TOTEM_OF_UNDYING;
    public static Material ANVIL_MATERIAL = (GoldenDupes.getInstance().serverVersion() <= 12) ? Material.valueOf("ANVIL") : Material.DAMAGED_ANVIL;

    public static boolean isShulkerBox(ItemStack i) {
        return i.getType().name().contains("SHULKER_BOX");
    }
}
