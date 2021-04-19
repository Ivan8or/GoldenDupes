package online.umbcraft.libraries;

import org.bukkit.Bukkit;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class GoldenDupes extends JavaPlugin implements Listener {

    // the amount of extra items to be given to the player
    final Map<UUID, Integer> dupeAmnt = new TreeMap<>();

    // the BukkitScheduler task ID of the most recent dupe reset event about to be run
    final Map<UUID, Integer> dupeTask = new TreeMap<>();

    // whether a player used the crafting autocomplete menu, or just clicked / drag clicked the item into the table
    final Map<UUID, Boolean> clickValidity = new TreeMap<>();


    // prevents the player from getting an extra item if they touch the crafting table normally
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickInv(final InventoryClickEvent e) {
        clickValidity.put(e.getWhoClicked().getUniqueId(), false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            clickValidity.remove(e.getWhoClicked().getUniqueId());
        }, 1L);
    }

    // prevents the player from getting an extra item if they touch the crafting table normally
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDragInv(final InventoryDragEvent e) {

        clickValidity.put(e.getWhoClicked().getUniqueId(), false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            clickValidity.remove(e.getWhoClicked().getUniqueId());
        }, 1L);
    }


    // increments the amount of duped items a player will receive, and checks to make sure they used the autocomplete
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(final PrepareItemCraftEvent e) {

        final UUID player_uuid = e.getView().getPlayer().getUniqueId();


        // no extra items if the player didn't autocomplete the recipe
        if(clickValidity.containsKey(player_uuid)) {
            return;
        }


        // increment the number of extra items by 2
        final int currentAmnt = dupeAmnt.getOrDefault(player_uuid, 0);
        dupeAmnt.put(player_uuid, currentAmnt+2);


        // prolongs the time until the extra items reset to 0 by one second
        cancelDupeClearTask(player_uuid);
        dupeTask.put(player_uuid,
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                dupeAmnt.remove(player_uuid);
                }, 20L));


        // makes the next click(s) this game tick not give extra items; needed due to how PrepareItemCraftEvent gets triggered
        clickValidity.put(player_uuid, false);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            clickValidity.remove(player_uuid);
        }, 0L);

    }

    // cancels an existing queued task which would set the player's extra item count to zero
    private void cancelDupeClearTask(final UUID player) {
        final int lastTask = dupeTask.getOrDefault(player, -1);
        if(Bukkit.getScheduler().isQueued(lastTask)) {
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

    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin) this);
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

        if (!(e.getEntity() instanceof Player))
            return;

        final Player p = (Player) e.getEntity();


        // players who didn't do the dupe are not affected
        if (!dupeAmnt.containsKey(p.getUniqueId())) {
            return;
        }


        final int dupe_amount = dupeAmnt.get(p.getUniqueId());
        final ItemStack stack = e.getItem().getItemStack();
        stack.setAmount(1);


        // maximum # of items gained at one time this way is 64
        final int final_amount = Math.min(64, dupe_amount);


        // adding the items one by one to prevent stacking normally-unstackable items like shulkers, etc.
        for(int i = 1; i < final_amount; i++) {
            p.getInventory().addItem(stack);
        }

        // player only dupes the first item he picks up
        dupeAmnt.remove(p.getUniqueId());
    }
}