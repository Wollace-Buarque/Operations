package dev.cromo29.operations.events;

import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.SpecificUtils.PlayerUtil;
import dev.cromo29.durkcore.Util.ParticleEffect;
import dev.cromo29.durkcore.Util.ParticleMaker;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.objects.Treasure;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureEvent implements Listener {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private final Map<String, Location> foundTreasureMap;

    public TreasureEvent(OperationPlugin plugin) {
        this.plugin = plugin;
        this.operationAPI = plugin.getOperationAPI();
        this.foundTreasureMap = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void interactEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        String treasureName = plugin.getConfig().getString("Settings.Treasure.Name");

        if (item == null
                || item.getType() != Material.WRITTEN_BOOK
                || !item.hasItemMeta()
                || !item.getItemMeta().hasDisplayName()
                || !item.getItemMeta().getDisplayName().equalsIgnoreCase(TXT.parse(treasureName))) return;

        event.setCancelled(true);

        Location treasureLocation = operationAPI.getLocationsGson().get("Treasure").asLocation();

        if (treasureLocation == null
                || !player.getWorld().getName().equalsIgnoreCase(treasureLocation.getWorld().getName())) return;

        if (!item.getItemMeta().hasLore()) return;

        List<String> lore = item.getItemMeta().getLore();

        try {

            String operationName = ChatColor.stripColor(lore.get(1).split(": ")[1]);
            int level = NumberUtil.getInt(ChatColor.stripColor(lore.get(2).split("Nível: ")[1]));
            int x = NumberUtil.getInt(ChatColor.stripColor(lore.get(3).split("X: ")[1]));
            int y = NumberUtil.getInt(ChatColor.stripColor(lore.get(4).split("Y: ")[1]));
            int z = NumberUtil.getInt(ChatColor.stripColor(lore.get(5).split("Z: ")[1]));

            Location chestLocation = new Location(treasureLocation.getWorld(), x, y, z);
            Location playerLocation = player.getLocation();

            if (x != playerLocation.getBlockX() || y != playerLocation.getBlockY() || z != playerLocation.getBlockZ()) {

                double distance = chestLocation.distance(playerLocation);

                if (distance <= 3) spawnChest(player, chestLocation, item, operationName, level);
                else PlayerUtil.sendActionBar(player, "<c>Você está à distância de <f>" + NumberUtil.formatNumberSimple(distance) + " <c>blocos do tesouro!");

            } else spawnChest(player, chestLocation, item, operationName, level);

        } catch (ArrayIndexOutOfBoundsException exception) {
            TXT.sendMessages(player, " <c>Ocorreu um erro ao tentar verificar o seu livro!");
        }
    }

    private void spawnChest(Player player, Location location, ItemStack item, String operationName, int level) {
        if (foundTreasureMap.containsKey(player.getName())) {
            sendMessage(player, " <c>Você precisa esperar o tesouro antigo sumir!");
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            return;
        }

        Treasure treasure = operationAPI.getTreasureManager().getTreasure(operationName, level);

        if (treasure == null) {
            sendMessage(player, "",
                    " <c>O nível do seu tesouro não foi encontrado!",
                    "");

            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            return;
        }

        if (location.getBlock().getType() == Material.CHEST) {
            sendMessage(player, "",
                    " <c>Já existe um baú nesta localização!",
                    " <c>Caso seja um tesouro, espere até ele sumir!",
                    " <c>Caso o baú não suma, peça para um staff remove-lo!",
                    "");

            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            return;
        }

        foundTreasureMap.put(player.getName(), location);

        Location playerLocation = player.getLocation();
        Vector vectorDirection = location.clone().toVector().subtract(playerLocation.clone().toVector()).normalize();

        playerLocation.setDirection(vectorDirection);
        player.teleport(playerLocation);

        location.getWorld().strikeLightningEffect(location);
        location.getWorld().strikeLightningEffect(location);

        location.getBlock().setType(Material.CHEST);

        Chest chest = (Chest) location.getBlock().getState();

        for (ItemStack itemStack : treasure.getItems()) {
            try {
                chest.getInventory().addItem(itemStack);
            } catch (ArrayIndexOutOfBoundsException exception) {
                plugin.log(" <c>Muitos itens no bau, foram adicionados apenas 27 itens.");
            }
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.setItemInHand(null);

        player.updateInventory();

        Vector vector = player.getEyeLocation().getDirection().normalize().multiply(-1);

        player.setVelocity(vector);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

        sendMessage(player, "",
                " <a>Você encontrou um tesouro nível <f>" + level + "<a>!",
                " <a>Este tesouro irá sumir em <f>1 minuto<a>!",
                "");

        new BukkitRunnable() {
            @Override
            public void run() {
                foundTreasureMap.remove(player.getName());

                if (location.getBlock().getType() == Material.CHEST) {
                    chest.getInventory().clear();
                    location.getBlock().setType(Material.AIR);
                }

                for (int i = 0; i < 5; i++)
                    ParticleMaker.sendParticle(ParticleEffect.REDSTONE, location.clone().add(0.5, 0.8, 0.5), Color.RED, 25, 20);

            }
        }.runTaskLater(plugin, 1200L);
    }

    void sendMessage(Player player, String... messages) {
        TXT.sendMessages(player, messages);
    }
}