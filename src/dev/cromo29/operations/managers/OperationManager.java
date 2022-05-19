package dev.cromo29.operations.managers;

import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.durkcore.Util.TextAnimation;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.utilitys.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

public class OperationManager {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private final PlayerOperationManager playerOperationManager;
    private final CollectorManager collectorManager;
    private final TreasureManager treasureManager;
    private final GuiManager guiManager;

    private TextAnimation textAnimation;
    private Villager villager;

    public OperationManager(OperationAPI operationAPI) {
        this.operationAPI = operationAPI;

        this.plugin = operationAPI.getPlugin();

        this.playerOperationManager = new PlayerOperationManager(operationAPI);
        this.collectorManager = new CollectorManager(operationAPI);
        this.treasureManager = new TreasureManager(operationAPI);
        this.guiManager = new GuiManager(operationAPI, playerOperationManager);
    }

    public void load() {
        loadOperations();

        playerOperationManager.setupPlayersOperations();
        playerOperationManager.loadPlayersOperations();

        spawnMob();

        treasureManager.loadTreasures();
        collectorManager.loadCollectors();
    }

    public Operation getOperationByName(String operationName) {
        for (Operation operation : operationAPI.getOperations()) {
            if (operation.getName().equalsIgnoreCase(operationName)) return operation;
        }

        return null;
    }

    public List<Operation> getOperations() {
        return operationAPI.getOperations();
    }

    public void spawnMob() {
        if (villager != null && !villager.isDead()) villager.remove();

        Location location = operationAPI.getLocationsGson().get("Mob").asLocation();

        if (location == null) return;

        Utils.getEntitiesInChunks(location, 1).forEach(entity -> {
            if (entity.getType() == EntityType.VILLAGER) entity.remove();
        });

        textAnimation = new TextAnimation(new TextAnimation.Hypixel(
                "VENDEDOR DE OPERAÇÕES",
                "<6><l>",
                "<f><l>",
                "<e><l>",
                "<6><l>",
                1,
                3,
                "<6><l>",
                "<f><l>",
                40));

        villager = location.getWorld().spawn(location, Villager.class);

        villager.setProfession(Villager.Profession.BLACKSMITH);
        villager.setAdult();
        villager.setCustomNameVisible(true);
        villager.setMetadata("operations", new FixedMetadataValue(plugin, "operations"));

        Utils.noAI(villager);
    }

    public void loadOperations() {
        operationAPI.getOperations().clear();

        ConfigurationSection operationsSection = operationAPI.getOperationsCFG().getSection("Operations");

        if (operationsSection == null) return;

        for (String name : operationsSection.getKeys(false)) {
            ConfigurationSection operationSection = operationsSection.getConfigurationSection(name);

            String display = operationSection.getString("Display");
            String icone = operationSection.getString("ID");
            String tag = operationSection.getString("Tag");
            long value = operationSection.getLong("Value");
            boolean enabled = operationSection.getBoolean("Enabled");

            List<String> types = operationSection.getStringList("Type");
            List<String> lore = new ArrayList<>();

            operationSection.getStringList("Lore")
                    .forEach(text -> lore.add(text.replace("{value}", "" + value)));

            Map<Operation.Type, Long> typesMap = new HashMap<>();

            int id, data = 0;
            if (icone.contains(":")) {
                id = NumberUtil.getInt(icone.split(":")[0]);
                data = NumberUtil.getInt(icone.split(":")[1]);
            } else id = NumberUtil.getInt(icone);

            for (String values : types) {
                String typeString = Operation.Type.valueOf(values.split(", ")[0].toUpperCase()).getName();
                Operation.Type type = null;

                for (Operation.Type typeOp : Operation.Type.values()) {

                    if (typeOp.getName().equalsIgnoreCase(typeString)) type = typeOp;
                }

                long amount = NumberUtil.getLong(values.split(", ")[1]);

                typesMap.put(type, amount);
            }

            ItemStack icon = new MakeItem(Material.WEB)
                    .setName(display)
                    .setLore(lore)
                    .setMaterial(id)
                    .setData(data)
                    .build();

            Operation operation = new Operation(name, tag, display, icon, value, enabled, lore, typesMap);
            operationAPI.getOperations().add(operation);
        }
    }


    public PlayerOperationManager getPlayerOperationManager() {
        return playerOperationManager;
    }

    public CollectorManager getCollectorManager() {
        return collectorManager;
    }

    public TreasureManager getTreasureManager() {
        return treasureManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }


    public Villager getVillager() {
        return villager;
    }

    public TextAnimation getTextAnimation() {
        return textAnimation;
    }
}
