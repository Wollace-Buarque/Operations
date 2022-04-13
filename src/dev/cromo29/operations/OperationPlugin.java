package dev.cromo29.operations;

import dev.cromo29.durkcore.API.DurkPlugin;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.commands.HuntCMD;
import dev.cromo29.operations.commands.OperationCMD;
import dev.cromo29.operations.events.OperationEvents;
import dev.cromo29.operations.events.TreasureEvent;
import dev.cromo29.operations.events.VillagerEvents;

public class OperationPlugin extends DurkPlugin {

    private final OperationAPI operationAPI;

    public OperationPlugin(OperationAPI operationAPI) {
        this.operationAPI = operationAPI;
    }

    @Override
    public void onStart() {
        registerCommands(new OperationCMD(operationAPI), new HuntCMD(this));
        setListeners(new OperationEvents(this), new TreasureEvent(this), new VillagerEvents(this));
    }

    @Override
    public void onStop() {
        operationAPI.getPlayerOperationMap().values().forEach(playerOperation -> {

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