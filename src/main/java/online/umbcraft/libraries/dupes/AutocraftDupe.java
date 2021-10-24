package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import online.umbcraft.libraries.schedule.DupeScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.*;

import static online.umbcraft.libraries.config.ConfigPath.*;

public class AutocraftDupe implements Listener {

    // super speedy set of all shulker items
    final private EnumSet<Material> shulkerBoxes;

    // the amount of extra items to be given to the player
    final private Map<UUID, Integer> dupeAmnt = new TreeMap<>();

    // the BukkitScheduler task ID of the most recent dupe reset event about to be run
    final private Map<UUID, Integer> dupeTask = new TreeMap<>();

    // whether a player used the crafting autocomplete menu, or just clicked / drag clicked the item into the table
    final private Map<UUID, Boolean> clickValidity = new TreeMap<>();

    final private GoldenDupes plugin;

    final private DupeScheduler autocraftScheduler;


    public AutocraftDupe(final GoldenDupes plugin) throws IOException {
        this.plugin = plugin;

        autocraftScheduler = new DupeScheduler(
                plugin,
                "autocraft",
                plugin.getConfig().getInt(AUTOCRAFT_ON.path()),
                plugin.getConfig().getInt(AUTOCRAFT_OFF.path())
        );


        // building an EnumSet of all shulkers
        shulkerBoxes = EnumSet.noneOf(Material.class);
        Arrays.stream(Material.values())
                .filter(m -> m.toString().endsWith("SHULKER_BOX"))
                .forEach(shulkerBoxes::add);
    }

    public DupeScheduler getScheduler() {
        return autocraftScheduler;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickInv(final InventoryClickEvent e) {

        if(!autocraftScheduler.isEnabled())
            return;

        denyDupeClick(e.getWhoClicked().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDragInv(final InventoryDragEvent e) {

        if(!autocraftScheduler.isEnabled())
            return;

        denyDupeClick(e.getWhoClicked().getUniqueId());
    }

    // prevents the player from getting an extra item if they touch the crafting table normally
    public void denyDupeClick(final UUID player) {
        clickValidity.put(player, false);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            clickValidity.remove(player);
        }, 1L);
    }


    // increments the amount of duped items a player will receive, and checks to make sure they used the autocomplete
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(final PrepareItemCraftEvent e) {

        if(!autocraftScheduler.isEnabled())
            return;

        final UUID playerUUID = e.getView().getPlayer().getUniqueId();


        // no extra items if the player didn't autocomplete the recipe
        if (clickValidity.containsKey(playerUUID)) {
            return;
        }


        // increment the number of extra items by 2
        final int currentAmnt = dupeAmnt.getOrDefault(playerUUID, 0);
        dupeAmnt.put(playerUUID, currentAmnt + 2);


        // prolongs the time until the extra items reset to 0 by one second
        cancelDupeClearTask(playerUUID);
        dupeTask.put(playerUUID,
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    dupeAmnt.remove(playerUUID);
                }, 50L));


        // makes the next click(s) this game tick not give extra items; needed due to how PrepareItemCraftEvent gets triggered
        clickValidity.put(playerUUID, false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            clickValidity.remove(playerUUID);
        }, 0L);

    }

    // cancels an existing queued task which would set the player's extra item count to zero
    private void cancelDupeClearTask(final UUID player) {
        final int lastTask = dupeTask.getOrDefault(player, -1);
        if (Bukkit.getScheduler().isQueued(lastTask)) {
            Bukkit.getScheduler().cancelTask(lastTask);
        }
        dupeTask.remove(player);
    }


    // player extra item count resets if they leave the menu
    @EventHandler(priority = EventPriority.HIGH)
    public void onCloseInv(final InventoryCloseEvent e) {

        dupeAmnt.remove(e.getPlayer().getUniqueId());
        cancelDupeClearTask(e.getPlayer().getUniqueId());
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(final PlayerKickEvent e) {

        dupeAmnt.remove(e.getPlayer().getUniqueId());
        dupeTask.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(final PlayerQuitEvent e) {

        dupeAmnt.remove(e.getPlayer().getUniqueId());
        dupeTask.remove(e.getPlayer().getUniqueId());
    }


    // gives the player the extra items once they pick up after performing the dupe
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(final EntityPickupItemEvent e) {

        if(!autocraftScheduler.isEnabled())
            return;

        // players who didn't do the dupe / other entities are not affected
        if (!dupeAmnt.containsKey(e.getEntity().getUniqueId())) {
            return;
        }

        final Player p = (Player) e.getEntity();
        final ItemStack stack = e.getItem().getItemStack();


        // set stacksize to 64 if vanilla, get correct size otherwise
        final int stacksize = (plugin.getConfig().getBoolean(ConfigPath.AUTOCRAFT_VANILLA.path()))
                ? 64 : decideAmount(stack.getType(), p.getUniqueId());

        // stacksize of 1 is a special case; this gives an extra item to the player without affecting the existing item

        final ItemStack toAdd = stack.clone();
        toAdd.setAmount(stacksize);
        p.getInventory().addItem(toAdd);

        if (stacksize != 1) { // this is a silly way to do it, but spigot api is poopy
            e.getItem().remove();
            e.setCancelled(true);
        }

        // player only tries to dupe the first item he picks up
        dupeAmnt.remove(p.getUniqueId());
    }


    // decides how much of a duped item a player receives
    private int decideAmount(final Material m, final UUID uuid) {

        final FileConfiguration config = plugin.getConfig();
        // defaults to the max amount of items the player can receive
        int playerEarned = Math.min(
                64,
                config.getInt(ConfigPath.AUTOCRAFT_MAX_ITEMS.path()));


        // if Items-Per-Click is enabled, uses the smaller between that value and the maximum
        if (config.getBoolean(ConfigPath.AUTOCRAFT_IPC.path()))
            playerEarned = Math.min(
                    playerEarned,
                    dupeAmnt.get(uuid) * config.getInt(ConfigPath.AUTOCRAFT_MULTIPLIER.path()));


        // the max stack size for unstackable items
        final int nonStackSize = config.getInt(ConfigPath.NON_STACK_STACKSIZE.path());

        // the max stack size for totems; defaults to the unstackable cap if set to less than 0
        final int totemConfig = config.getInt(ConfigPath.TOTEMS_STACKSIZE.path());
        final int totemSize = (totemConfig < 0) ? nonStackSize : totemConfig;

        // the max stack size for shulkers; defaults to the unstackable cap if set to less than 0
        final int shulkerConfig = config.getInt(ConfigPath.SHULKERS_STACKSIZE.path());
        final int shulkerSize = (shulkerConfig < 0) ? nonStackSize : shulkerConfig;


        // if the item is a totem, uses the smaller of what the player should get, and the maximum totem size
        if (m == Material.TOTEM_OF_UNDYING)
            return (config.getBoolean(ConfigPath.TOTEMS_DO_DUPE.path())) ?
                    Math.min(playerEarned, totemSize) : 0;


        // if the item is a shulker box, uses the smaller of what the player should get, and the maximum shulker size
        if (shulkerBoxes.contains(m))
            return (config.getBoolean(ConfigPath.SHULKERS_DO_DUPE.path())) ?
                    Math.min(playerEarned, shulkerSize) : 0;


        // if the item is any other nonstackable, uses the smaller of what the player should get, and the maximum stackable size
        if (m.getMaxStackSize() != 64)
            return (config.getBoolean(ConfigPath.NON_STACK_DO_DUPE.path())) ?
                    Math.min(playerEarned, nonStackSize) : 0;


        // if the item is stackable to 64, returns the regular amount earned by the player
        return playerEarned;
    }


}
