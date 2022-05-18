package dev.cromo29.operations.managers;

import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.Util.ConfigManager;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.objects.Treasure;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.utilitys.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreasureManager {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private final Random random;

    public TreasureManager(OperationPlugin plugin) {
        this.plugin = plugin;

        this.operationAPI = plugin.getOperationAPI();

        this.random = new Random();
    }

    public List<Treasure> getTreasuresFrom(String operationName) {
        List<Treasure> treasures = new ArrayList<>();
        for (Treasure treasure : operationAPI.getTreasures()) {
            if (treasure.getOperation().getName().equalsIgnoreCase(operationName)) treasures.add(treasure);
        }

        return treasures;
    }

    public Treasure getTreasure(String operationName, int level) {
        for (Treasure treasure : getTreasuresFrom(operationName)) {
            if (treasure.getLevel() == level) return treasure;
        }

        return null;
    }

    public Treasure getRandomTreasure(String operationName, int min, int max) {

        if (getTreasuresFrom(operationName).isEmpty()) {
            plugin.log(" <c>Nao existe nenhum tesouro na operacao <f>" + operationName + "<c>!");
            return null;
        }

        for (int level = min; level < max; level++) {

            if (getTreasure(operationName, level) == null) {
                plugin.log(" <c>Nao existe nenhum tesouro nivel <f>" + level + "<c>!");
                return null;
            }

        }

        final double random = this.random.nextDouble() * 100;
        for (Treasure treasure : getTreasuresFrom(operationName)) {

            if (random >= treasure.getMinPercentage() && random < treasure.getMaxPercentage()) return treasure;

        }

        return null;
    }

    public void loadTreasures() {
        operationAPI.getTreasures().clear();

        ConfigManager treasuresCFG = operationAPI.getTreasuresCFG();

        if (treasuresCFG.getConfigurationSection("Treasures") == null) return;

        for (String operationName : treasuresCFG.getConfigurationSection("Treasures")) {

            Operation operation = operationAPI.getOperationManager().getOperationByName(operationName);

            if (operation == null) {
                plugin.log(" <c>A operacao <f>" + operationName + " <c>nao foi encontrada!");
                continue;
            }

            double totalPercentage = 0;
            for (String level : treasuresCFG.getConfigurationSection("Treasures." + operationName)) {
                boolean error = false;
                String path = "Treasures." + operationName + "." + level;

                double percentage = treasuresCFG.getDouble(path + ".Percentage");

                List<ItemStack> diceList = new ArrayList<>();
                for (String item : treasuresCFG.getStringList(path + ".Items")) {

                    try {
                        String itemID = item.split(";")[0];
                        String itemName = item.split(";")[1].replace("&", "§");
                        String enchantments = item.split(";")[2];
                        String amount = item.split(";")[3];
                        String lore = item.split(";")[4].replace("&", "§");

                        boolean hasName = !itemName.equalsIgnoreCase("null");
                        boolean hasEnchantments = !enchantments.equalsIgnoreCase("null");
                        boolean hasLore = !lore.equalsIgnoreCase("null");

                        int data = 0;
                        if (itemID.contains(":")) {
                            data = Integer.parseInt(itemID.split(":")[1]);
                            itemID = itemID.split(":")[0];
                        }

                        ItemStack itemStack = new MakeItem(Material.WEB)
                                .setMaterial(Integer.parseInt(itemID))
                                .setAmount(Integer.parseInt(amount))
                                .setData(data)
                                .build();

                        ItemMeta itemMeta = itemStack.getItemMeta();

                        if (hasName) itemMeta.setDisplayName(itemName);

                        if (hasEnchantments) {
                            if (enchantments.contains("-")) {

                                for (String enchantString : enchantments.split("-")) {
                                    String enchantName = enchantString.split(":")[0];
                                    int enchantLevel = Integer.parseInt(enchantString.split(":")[1]);

                                    Enchantment enchantment = Utils.translateEnchant(enchantName);

                                    if (enchantment != null) itemMeta.addEnchant(enchantment, enchantLevel, true);
                                }

                            } else {
                                String enchantName = enchantments.split(":")[0];
                                int enchantLevel = Integer.parseInt(enchantments.split(":")[1]);

                                Enchantment enchantment = Utils.translateEnchant(enchantName);

                                if (enchantment != null) itemMeta.addEnchant(enchantment, enchantLevel, true);
                            }
                        }

                        if (hasLore) {
                            List<String> itemlore = new ArrayList<>();

                            for (String text : lore.split(":")) {
                                if (text == null) continue;

                                itemlore.add(text);
                            }

                            itemMeta.setLore(itemlore);
                        }

                        itemStack.setItemMeta(itemMeta);
                        diceList.add(itemStack);

                    } catch (Exception exception) {
                        plugin.logs(
                                " <c>Erro no tesouro <f>" + level + " <c>da operacao <f>" + operationName + "<c>.",
                                " <c>Item: <f>" + item);

                        error = true;
                    }
                }

                if (error) {
                    plugin.log(" <c>Removendo o tesouro <f>" + level + " <c>da operacao <f>" + operationName + "<c>.");
                    continue;
                }

                Treasure treasure = new Treasure(operation, NumberUtil.getInt(level), percentage, diceList);

                treasure.setMinPercentage(totalPercentage);

                totalPercentage += treasure.getPercentage();

                treasure.setMaxPercentage(totalPercentage);

                if (operationAPI.getTreasures().contains(treasure)) {
                    plugin.log(" <c>Ja existe um tesouro nivel <f>" + level + " <c>na operacao <f>" + operationName + "<c>.");
                    continue;
                }

                operationAPI.getTreasures().add(treasure);
            }

            if (totalPercentage != 100) {
                operationAPI.getTreasures().removeIf(treasureFilter -> treasureFilter.getOperation().getName().equalsIgnoreCase(operationName));

                plugin.logs("",
                        " <c>A porcentagem total nao era igual a <f>100%<c>, tesouros removidos.",
                        "");
                break;
            }
        }
    }
}
