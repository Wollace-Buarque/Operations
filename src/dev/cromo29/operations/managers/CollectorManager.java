package dev.cromo29.operations.managers;

import dev.cromo29.durkcore.SpecificUtils.ItemUtil;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.utilitys.ScrollerInventory;
import dev.cromo29.operations.objects.Collector;
import dev.cromo29.operations.OperationPlugin;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CollectorManager {

    private final OperationPlugin plugin;
    private final OperationAPI operationAPI;

    public CollectorManager(OperationPlugin plugin) {
        this.plugin = plugin;
        this.operationAPI = plugin.getOperationAPI();
    }

    public void openCollector(Player player) {
        Collector collector = getCollector(player.getName());

        if (collector == null) {
            sendMessage(player, " <c>Você não tem nenhum item para coletar!");
            return;
        }

        ScrollerInventory inventory = new ScrollerInventory(collector.getItems(), "<b>Coletar premios:", player,
                43, true, true, true, true, event -> {

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            if (player.getInventory().firstEmpty() == -1) {
                sendMessage(player,
                        "",
                        " <c>O seu inventário está cheio!",
                        "");
                return;
            }

            ItemStack item = event.getCurrentItem();

            collector.getItems().remove(item);

            player.getInventory().addItem(item);
            player.updateInventory();

            String name = item.getItemMeta().hasDisplayName() ?
                    item.getItemMeta().getDisplayName()
                    :
                    StringUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));

            deleteItem(ItemUtil.toString(item));

            event.setCurrentItem(null);

            sendMessage(player, "",
                    " <a>Você coletou <b>x" + item.getAmount() + " " + name + "<a>!",
                    "");

            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

            if (collector.getItems().isEmpty()) {
                sendMessage(player, "", " <a>Não há mais itens para coletar!", "");

                player.closeInventory();
                player.playSound(player.getLocation(), Sound.CHEST_CLOSE, 1, 1);

                operationAPI.getPlayerCollectorMap().remove(player.getName().toLowerCase());
                return;
            }

            if (event.getSlot() == 10) {
                boolean close = true;

                for (int i = 0; i < 43; i++) {
                    if (event.getInventory().getItem(i) != null && event.getInventory().getItem(i).getType() != Material.AIR) {
                        close = false;
                        break;
                    }
                }

                if (close) player.closeInventory();

            }
        });

        inventory.open(player);

        sendMessage(player, "<a>Você abriu o menu de coletas!");
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
    }

    public void sendCollector(Player player, ItemStack item) {
        Collector collector = getCollector(player.getName());

        if (collector == null) {
            collector = new Collector(player.getName(), new ArrayList<>());

            operationAPI.getPlayerCollectorMap().put(player.getName().toLowerCase(), collector);
        }

        collector.addItems(item);
        saveItem(player, item);

        String name = item.getItemMeta().hasDisplayName() ?
                item.getItemMeta().getDisplayName()
                :
                StringUtils.capitalize(item.getType().name().toLowerCase().replace("_", " "));

        sendMessage(player,
                "",
                " " + name + " <a>adicionado ao seu menu de coleta!",
                " <a>Para pega-lo digite: <f>/operação coletar<a>!",
                "");
    }

    public Collector getCollector(String playerName) {
        return operationAPI.getPlayerCollectorMap().get(playerName.toLowerCase());
    }

    void sendMessage(Player player, String... messages) {
        TXT.sendMessages(player, messages);
    }

    public void deleteItem(String jsonItem) {
        TXT.runAsynchronously(plugin, () -> {

            for (String user : operationAPI.getCollectorCFG().getConfigurationSection("Collector")) {

                String path = "Collector." + user + ".Items";

                int size = 0;
                for (String id : operationAPI.getCollectorCFG().getConfigurationSection(path)) {
                    size++;

                    String itemString = operationAPI.getCollectorCFG().getString("Collector." + user + ".Items." + id);

                    if (itemString.equalsIgnoreCase(jsonItem)) {
                        size -= 1;

                        operationAPI.getCollectorCFG().set(path + "." + id, null);
                        break;
                    }
                }

                if (size == 0) operationAPI.getCollectorCFG().set("Collector." + user, null);

                operationAPI.getCollectorCFG().save();
            }

        });
    }

    public void saveItem(Player player, ItemStack item) {
        TXT.runAsynchronously(plugin, () -> {

            boolean doUpdate = false;
            for (String user : operationAPI.getCollectorCFG().getConfigurationSection("Collector")) {

                if (player.getName().equalsIgnoreCase(user)) {
                    doUpdate = true;

                    String path = "Collector." + user + ".Items";

                    if (operationAPI.getCollectorCFG().getConfigurationSection(path) == null) {
                        operationAPI.getCollectorCFG().setItemStackS(path + ".1", item);
                    } else {
                        int index = operationAPI.getCollectorCFG().getConfigurationSection(path).size() + 1;

                        operationAPI.getCollectorCFG().setItemStackS(path + "." + index, item);
                    }

                    operationAPI.getCollectorCFG().save();
                    break;
                }
            }

            if (!doUpdate) {
                operationAPI.getCollectorCFG().setItemStackS("Collector." + player.getName().toLowerCase() + ".Items.1", item);
                operationAPI.getCollectorCFG().save();
            }

        });
    }

    public void loadCollectors() {
        operationAPI.getPlayerCollectorMap().clear();

        if (operationAPI.getCollectorCFG().getConfigurationSection("Collector") == null) return;

        for (String user : operationAPI.getCollectorCFG().getConfigurationSection("Collector")) {

            List<ItemStack> items = new ArrayList<>();

            String path = "Collector." + user + ".Items";
            for (String id : operationAPI.getCollectorCFG().getConfigurationSection(path)) {

                try {
                    ItemStack item = operationAPI.getCollectorCFG().getItemStackS(path + "." + id);

                    items.add(item);
                } catch (Exception exception) {
                    plugin.log(" <c>Ocorreu um erro no item de <f>" + user + "<c>: <f>" + id);
                    items.remove(operationAPI.getCollectorCFG().getItemStackS(path + "." + id));
                }

                Collector collector = new Collector(user, items);
                operationAPI.getPlayerCollectorMap().put(user.toLowerCase(), collector);
            }
        }
    }
}
