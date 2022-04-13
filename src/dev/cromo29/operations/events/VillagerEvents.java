package dev.cromo29.operations.events;

import dev.cromo29.durkcore.Updater.event.UpdaterEvent;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.OperationPlugin;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class VillagerEvents implements Listener {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private int counter;

    public VillagerEvents(OperationPlugin plugin) {
        this.plugin = plugin;
        this.operationAPI = plugin.getOperationAPI();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interactEvent(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (event.getRightClicked() == null || event.getRightClicked().getType() != EntityType.VILLAGER) return;

        if (!event.getRightClicked().hasMetadata("operations")) return;

        event.setCancelled(true);

        player.closeInventory();

        operationAPI.getGuiManager().showOperations(player);
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Villager)) return;

        if (event.getEntity().hasMetadata("operations")) event.setCancelled(true);
    }

    @EventHandler
    public void damageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!event.getEntity().hasMetadata("operations")) return;

        event.setCancelled(true);

        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();

        if (damager.hasPermission("29Operations.*") && damager.getItemInHand().getType() == Material.STICK) {
            event.getEntity().remove();

            TXT.runAsynchronously(plugin, () -> {
                operationAPI.getLocationsGson().remove("Mob");
                operationAPI.getLocationsGson().save();
            });

            TXT.sendMessages(damager, " <a>Você removeu o aldeão das operaçãos!");

        } else operationAPI.getGuiManager().showOperations(damager);
    }

    @EventHandler
    public void updateEvent(UpdaterEvent event) {
        final Villager villager = operationAPI.getOperationManager().getVillager();

        if (villager == null || villager.isDead()) return;

        counter++;

        if (counter % 3 == 0) villager.setCustomName(operationAPI.getOperationManager().getTextAnimation().next());
        if (counter >= 100) counter = 0;
    }
}
