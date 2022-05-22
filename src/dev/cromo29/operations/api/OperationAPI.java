package dev.cromo29.operations.api;

import dev.cromo29.durkcore.util.ConfigManager;
import dev.cromo29.durkcore.util.GsonManager;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.managers.*;
import dev.cromo29.operations.objects.Collector;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.objects.PlayerOperation;
import dev.cromo29.operations.objects.Treasure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationAPI {

    private final OperationPlugin plugin;

    private Map<String, PlayerOperation> playerOperationMap;
    private Map<String, Collector> playerCollectorMap;

    private List<Operation> operations;
    private List<Treasure> treasures;

    private ConfigManager operationsFile, playerOperationsFile, medalsFile, treasuresFile, collectorsFile;
    private GsonManager locationsGson;

    private OperationManager operationManager;

    public OperationAPI(OperationPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    public void reloadAll() {
        plugin.reloadConfig();

        locationsGson.reload();

        medalsFile.reload();
        treasuresFile.reload();
        playerOperationsFile.reload();
        operationsFile.reload();

        operationManager.load();

        operationManager.spawnMob();
    }

    private void init() {
        playerOperationMap = new HashMap<>();
        playerCollectorMap = new HashMap<>();

        operations = new ArrayList<>();
        treasures = new ArrayList<>();

        loadConfigs();

        operationManager = new OperationManager(this);
        operationManager.load();
    }

    private void loadConfigs() {
        final String storagePath = plugin.getDataFolder().getPath() + File.separator + "storage";

        operationsFile = new ConfigManager(plugin, "operations.yml");
        treasuresFile = new ConfigManager(plugin, "treasures.yml");

        playerOperationsFile = new ConfigManager(storagePath, "progress.yml");
        medalsFile = new ConfigManager(storagePath, "medals.yml");
        collectorsFile = new ConfigManager(storagePath, "collector.yml");
        locationsGson = new GsonManager(storagePath, "locations.json").prepareGson();

        plugin.saveDefaultConfig();
    }

    public Map<String, PlayerOperation> getPlayerOperationMap() {
        return playerOperationMap;
    }

    public Map<String, Collector> getPlayerCollectorMap() {
        return playerCollectorMap;
    }


    public List<Operation> getOperations() {
        return operations;
    }

    public List<Treasure> getTreasures() {
        return treasures;
    }


    public ConfigManager getOperationsCFG() {
        return operationsFile;
    }

    public ConfigManager getPlayerOperationsCFG() {
        return playerOperationsFile;
    }

    public ConfigManager getMedalsCFG() {
        return medalsFile;
    }

    public ConfigManager getTreasuresCFG() {
        return treasuresFile;
    }

    public ConfigManager getCollectorCFG() {
        return collectorsFile;
    }


    public GsonManager getLocationsGson() {
        return locationsGson;
    }


    public OperationManager getOperationManager() {
        return operationManager;
    }

    public PlayerOperationManager getPlayerOperationManager() {
        return operationManager.getPlayerOperationManager();
    }

    public TreasureManager getTreasureManager() {
        return operationManager.getTreasureManager();
    }

    public CollectorManager getCollectorManager() {
        return operationManager.getCollectorManager();
    }

    public GuiManager getGuiManager() {
        return operationManager.getGuiManager();
    }


    public OperationPlugin getPlugin() {
        return plugin;
    }
}
