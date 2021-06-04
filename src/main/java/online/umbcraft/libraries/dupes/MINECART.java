package br.com.nao;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.EventPriority;
import  org.bukkit.entity.Entity ;
import  org.bukkit.entity.EntityType ;
import  org.bukkit.event.EventHandler ;
import  org.bukkit.event.Listener ;
import org.bukkit.inventory.Inventory;

public class minecartdupe implements Listener {

    final private minecartdupe plugin;
    
    //entidade entra no portal
    
    @EventHandler
    
    EntityPortalEnterEvent
   Entity entity = b.getEntity();
    if(entity.getType() == EntityType.STORAGE_MINECART) {
    	
    }
    
    @EventHandler (priority = EventPriority.HIGH)
    public void EntityPortalEnterEvent (final PlayerQuitEvent e) {

        final Entity vehicle = e.getPlayer().getVehicle();

        if(plugin.getConfig().getBoolean(ConfigPath.STORAGE_MINECART.name()))
            traverseBoat(vehicle);
        else
            dupeInventory(vehicle);
    }
    	
 
    @EventHandler
    
    {  private void dupeInventory(final Entity riding) {

        if (!(riding instanceof AbstractHorse))
            return;

        final AbstractHorse donkey = (AbstractHorse) riding;
        final Inventory cloned = clone(donkey);
        final List<HumanEntity> viewers = donkey.getInventory().getViewers();

        // weird iteration because iterators gave too much trouble
        for (int i = viewers.size() - 1; i >= 0; i--) {
            final HumanEntity human = viewers.get(i);
            human.closeInventory();
            human.openInventory(cloned);
        }
    }
}
    
    private Inventory clone(final AbstractHorse donkey) {

        final Inventory toClone = donkey.getInventory();
        final Inventory result = Bukkit.createInventory(null, toClone.getType());

        for (int i = 0; i <= 16; i++) {
            result.setItem(i, toClone.getItem(i));
        }
        return result;
    }
}
