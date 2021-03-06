package dev.cromo29.operations;

import dev.cromo29.durkcore.api.DurkPlugin;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.commands.HuntCMD;
import dev.cromo29.operations.commands.OperationCMD;
import dev.cromo29.operations.events.OperationEvents;
import dev.cromo29.operations.events.TreasureEvent;
import dev.cromo29.operations.events.VillagerEvents;
import dev.cromo29.operations.objects.PlayerOperation;

import java.util.Map;

public class OperationPlugin extends DurkPlugin {

    private OperationAPI operationAPI;

    @Override
    public void onStart() {
        this.operationAPI = new OperationAPI(this);

        registerCommands(new OperationCMD(operationAPI), new HuntCMD(this));
        setListeners(new OperationEvents(this), new TreasureEvent(this), new VillagerEvents(operationAPI));
    }

    @Override
    public void onStop() {
        Map<String, PlayerOperation> playerOperationMap = operationAPI.getPlayerOperationMap();

        if (playerOperationMap == null) return;

        playerOperationMap.values().forEach(playerOperation -> {

            if (playerOperation.getOperations().isEmpty()) return;

            operationAPI.getPlayerOperationManager().savePlayerOperations(playerOperation);
        });
    }


    public OperationAPI getOperationAPI() {
        return operationAPI;
    }


    public static OperationPlugin get() {
        return getPlugin(OperationPlugin.class);
    }
}