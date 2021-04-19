package online.umbcraft.libraries;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class GoldenDupes extends JavaPlugin implements Listener {

    final Map<UUID, Integer> dupeAmnt = new TreeMap<>();
    final Map<UUID, Integer> dupeTask = new TreeMap<>();
    final Map<UUID, Boolean> dupeSpeedLimit = new TreeMap<>();


    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(CraftItemEvent e) {
        Bukkit.broadcastMessage("Crafted item!");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCraftItem(PrepareItemCraftEvent e) {

        if(e.getRecipe() == null)
            return;
        e.getView().getPlayer().sendMessage("recipe is "+e.getRecipe().getResult());

        final UUID player_uuid = e.getView().getPlayer().getUniqueId();
        if(dupeSpeedLimit.containsKey(player_uuid))
            return;

        dupeSpeedLimit.put(player_uuid, true);
        int currentAmnt = dupeAmnt.getOrDefault(player_uuid, 0);
        dupeAmnt.put(player_uuid, currentAmnt+2);
        cancelDupeClearTask(player_uuid);

        int newTask = Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            dupeAmnt.remove(player_uuid);
        }, 20L);

        dupeTask.put(player_uuid, newTask);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            dupeSpeedLimit.remove(player_uuid);
        }, 0L);

    }

    private void cancelDupeClearTask(UUID player) {
        int lastTask = dupeTask.getOrDefault(player, -1);
        if(Bukkit.getScheduler().isQueued(lastTask)) {
            Bukkit.getScheduler().cancelTask(lastTask);
        }
        dupeTask.remove(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCloseInv(InventoryCloseEvent e) {
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
    public void onPlayerKick(PlayerKickEvent e) {
        dupeAmnt.remove(e.getPlayer().getUniqueId());
        dupeTask.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeave(PlayerQuitEvent e) {
        dupeAmnt.remove(e.getPlayer().getUniqueId());
        dupeTask.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(final EntityPickupItemEvent e) {

        if (!(e.getEntity() instanceof Player))
            return;

        final Player p = (Player) e.getEntity();

        if (!dupeAmnt.containsKey(p.getUniqueId())) {
            return;
        }

        int dupe_amount = dupeAmnt.get(p.getUniqueId());
        final ItemStack stack = e.getItem().getItemStack();
        stack.setAmount(1);
        final int final_amount = Math.min(64, dupe_amount);

        for(int i = 1; i < final_amount; i++) {
            p.getInventory().addItem(stack);
        }

        dupeAmnt.remove(p.getUniqueId());
    }
}