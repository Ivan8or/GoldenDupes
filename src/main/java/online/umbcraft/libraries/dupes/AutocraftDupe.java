package online.umbcraft.libraries.dupes;

import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
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

import java.util.*;

public class AutocraftDupe extends Dupe implements Listener {

    // the amount of extra items to be given to the player
    final private Map<UUID, Integer> dupeAmnt = new TreeMap<>();

    // the BukkitScheduler task ID of the most recent dupe reset event about to be run
    final private Map<UUID, Integer> dupeTask = new TreeMap<>();

    // whether a player used the crafting autocomplete menu, or just clicked / drag clicked the item into the table
    final private Map<UUID, Boolean> clickValidity = new TreeMap<>();

    public AutocraftDupe(final GoldenDupes plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickInv(final InventoryClickEvent e) {
        denyDupeClick(e.getWhoClicked().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDragInv(final InventoryDragEvent e) {

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

        final UUID playerUUID = e.getView().getPlayer().getUniqueId();


        // no extra items if the player didn't autocomplete the recipe
        if (clickValidity.containsKey(playerUUID)) {
            return;
        }


        // increment the number of extra items
        final int currentAmnt = dupeAmnt.getOrDefault(playerUUID, 0);
        dupeAmnt.put(playerUUID, currentAmnt+1);


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
    @EventHandler(priority = EventPriority.LOW)
    public void onItemPickup(final EntityPickupItemEvent e) {

        // players who didn't do the dupe / other entities are not affected
        if (!dupeAmnt.containsKey(e.getEntity().getUniqueId())) {
            return;
        }

        final ItemStack toDupe = e.getItem().getItemStack();
        final Player p = (Player) e.getEntity();

        // set stacksize to 64 if vanilla, get correct size otherwise
        final int stacksize = decideAmount(toDupe.getType(), p.getUniqueId());

        final ItemStack duped = dupe(toDupe, stacksize);

        e.getItem().setItemStack(null);
        e.setCancelled(true);
        int currentMaxStack = p.getInventory().getMaxStackSize();
        int newMaxStack = newAmount(toDupe, stacksize);

        p.getInventory().setMaxStackSize(Math.max(currentMaxStack, newMaxStack));
        p.getInventory().addItem(duped);

        // player only tries to dupe the first item he picks up
        dupeAmnt.remove(p.getUniqueId());
    }


    // decides how much of a duped item a player receives
    private int decideAmount(final Material m, final UUID uuid) {

        final FileConfiguration config = plugin.getConfig();

        if(plugin.getConfig().getBoolean(ConfigPath.AUTOCRAFT_VANILLA.path()))
            return 64;

        // defaults to the max amount of items the player can receive
        int playerEarned = config.getInt(ConfigPath.AUTOCRAFT_MAX_ITEMS.path());


        // if Items-Per-Click is enabled, uses the smaller between that value and the maximum
        if (config.getBoolean(ConfigPath.AUTOCRAFT_IPC.path()))
            playerEarned = Math.min(
                    playerEarned,
                    dupeAmnt.get(uuid) * config.getInt(ConfigPath.AUTOCRAFT_MULTIPLIER.path()));

        // if the item is stackable to 64, returns the regular amount earned by the player
        return playerEarned;
    }


}
