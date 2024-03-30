package online.umbcraft.libraries.dupes;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import online.umbcraft.libraries.GoldenDupes;
import online.umbcraft.libraries.config.ConfigPath;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static io.papermc.paper.threadedregions.scheduler.ScheduledTask.ExecutionState.IDLE;

public class AutocraftDupe extends Dupe implements Listener {

    // the amount of extra items to be given to the player
    final private Map<UUID, Integer> dupeAmnt = new TreeMap<>();

    // the BukkitScheduler task ID of the most recent dupe reset event about to be run
    final private Map<UUID, Integer> dupeTask = new TreeMap<>();

    // the FoliaScheduler task of the most recent dupe reset event about to be run
    final private Map<UUID, ScheduledTask> dupeTask_Folia = new TreeMap<>();

    // whether a player used the crafting autocomplete menu, or just clicked / drag clicked the item into the table
    final private Map<UUID, Boolean> clickValidity = new TreeMap<>();

    public AutocraftDupe() {
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

        if (GoldenDupes.isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(GoldenDupes.getInstance(), t -> {
                clickValidity.remove(player);
            }, 1L);
        }
        else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), () -> {
                clickValidity.remove(player);
            }, 1L);
        }

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
        if (GoldenDupes.isFolia) {
            dupeTask_Folia.put(playerUUID,
                    Bukkit.getGlobalRegionScheduler().runDelayed(GoldenDupes.getInstance(), t -> {
                        dupeAmnt.remove(playerUUID);
                    }, 50L));
        }
        else {
            dupeTask.put(playerUUID,
                    Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), () -> {
                        dupeAmnt.remove(playerUUID);
                    }, 50L));
        }

        // makes the next click(s) this game tick not give extra items; needed due to how PrepareItemCraftEvent gets triggered
        clickValidity.put(playerUUID, false);
        if (GoldenDupes.isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(GoldenDupes.getInstance(), t -> {
                clickValidity.remove(playerUUID);
            }, 1L);
        }
        else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(GoldenDupes.getInstance(), () -> {
                clickValidity.remove(playerUUID);
            }, 0L);
        }


    }

    // cancels an existing queued task which would set the player's extra item count to zero
    private void cancelDupeClearTask(final UUID player) {
        if (GoldenDupes.isFolia) {
            final ScheduledTask lastTask = dupeTask_Folia.getOrDefault(player, null);
            if (lastTask != null) {
                if (lastTask.getExecutionState().toString().equals("IDLE")) {
                    lastTask.cancel();
                }
                dupeTask_Folia.remove(player);
            }
        }
        else {
            final int lastTask = dupeTask.getOrDefault(player, -1);
            if (Bukkit.getScheduler().isQueued(lastTask)) {
                Bukkit.getScheduler().cancelTask(lastTask);
            }
            dupeTask.remove(player);
        }
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
        if (GoldenDupes.isFolia) {
            dupeTask_Folia.remove(e.getPlayer().getUniqueId());
        }
        else {
            dupeTask.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(final PlayerQuitEvent e) {
        dupeAmnt.remove(e.getPlayer().getUniqueId());
        if (GoldenDupes.isFolia) {
            dupeTask_Folia.remove(e.getPlayer().getUniqueId());
}
        else {
            dupeTask.remove(e.getPlayer().getUniqueId());
        }
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

        e.setCancelled(true);
        int currentMaxStack = p.getInventory().getMaxStackSize();
        int newMaxStack = newAmount(toDupe, stacksize);

        p.getInventory().setMaxStackSize(Math.max(currentMaxStack, newMaxStack));

        if (GoldenDupes.isFolia) {
            Bukkit.getRegionScheduler().run(GoldenDupes.getInstance(), p.getLocation(), t -> {
                Item onGround = p.getLocation().getWorld().dropItem(p.getLocation(), duped);
                onGround.setPickupDelay(1);
            });
        }
        else {
            Item onGround = p.getLocation().getWorld().dropItem(p.getLocation(), duped);
            onGround.setPickupDelay(1);
        }


        // player only tries to dupe the first item he picks up
        dupeAmnt.remove(p.getUniqueId());
    }


    // decides how much of a duped item a player receives
    private int decideAmount(final Material m, final UUID uuid) {

        final FileConfiguration config = GoldenDupes.getInstance().getConfig();

        if(GoldenDupes.getInstance().getConfig().getBoolean(ConfigPath.AUTOCRAFT_VANILLA.path()))
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
