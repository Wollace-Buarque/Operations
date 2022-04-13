package dev.cromo29.operations.managers;

import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.objects.PlayerOperation;
import dev.cromo29.operations.objects.ProgressOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerOperationManager {

    private final OperationAPI operationAPI;

    private final OperationManager operationManager;

    public PlayerOperationManager(OperationAPI operationAPI) {
        this.operationAPI = operationAPI;

        this.operationManager = operationAPI.getOperationManager();
    }

    public List<Operation> getPlayerOperations(String playerName) {
        final PlayerOperation playerOperation = getPlayerOperation(playerName);

        return playerOperation == null ? new ArrayList<>() : playerOperation.getOperations();
    }

    public PlayerOperation getPlayerOperation(String playerName) {
        return operationAPI.getPlayerOperationMap().get(playerName.toLowerCase());
    }

    public ProgressOperation getProgressOperation(PlayerOperation playerOperation, String operationName) {
        return playerOperation.getProgressOperations()
                .stream()
                .filter(progressOperation -> progressOperation.getOperation().getName().equalsIgnoreCase(operationName))
                .findFirst().orElse(null);
    }

    public void savePlayerOperations(PlayerOperation playerOperation) {
        String user = playerOperation.getUser().toLowerCase();

        Operation using = playerOperation.getCurrent();

        List<Operation> operations = playerOperation.getOperations();
        List<Operation> finisheds = playerOperation.getFinished();
        List<ProgressOperation> progressOperations = playerOperation.getProgressOperations();

        String medals = TXT.createString(operations.stream().map(Operation::getName).toArray(), 0, ", ");

        for (ProgressOperation progressOperation : progressOperations) {

            List<String> preFinishedList = new ArrayList<>();
            for (Operation.Type type : progressOperation.getPreFinished()) {
                if (playerOperation.hasFinished(progressOperation.getOperation())) break;

                preFinishedList.add(type.getReplacedName());
            }

            List<String> progressList = new ArrayList<>();
            for (Map.Entry<Operation.Type, Long> entry : progressOperation.getProgress().entrySet()) {
                if (playerOperation.hasFinished(progressOperation.getOperation())) break;

                if (!preFinishedList.contains(entry.getKey().getReplacedName())) {
                    progressList.add(entry.getKey().getReplacedName() + ", " + entry.getValue());
                }
            }

            String operationName = progressOperation.getOperation().getName().toLowerCase();

            if (!progressList.isEmpty())
                operationAPI.getPlayerOperationsCFG().set("Accounts." + user + "." + operationName + ".Progress", progressList);

            if (!preFinishedList.isEmpty())
                operationAPI.getPlayerOperationsCFG().set("Accounts." + user + "." + operationName + ".Pre-Finished", preFinishedList);

            if (!progressList.isEmpty() || !preFinishedList.isEmpty()) operationAPI.getPlayerOperationsCFG().save();
        }

        String finishedsString = TXT.createString(finisheds.stream().map(Operation::getName).toArray(), 0, ", ");

        if (using != null) operationAPI.getMedalsCFG().set("Accounts." + user + ".Using", using.getName());

        if (!operations.isEmpty()) operationAPI.getMedalsCFG().set("Accounts." + user + ".Medals", medals);

        if (!finisheds.isEmpty()) operationAPI.getMedalsCFG().set("Accounts." + user + ".Finisheds", finishedsString);

        operationAPI.getPlayerOperationsCFG().save();
        operationAPI.getMedalsCFG().save();
    }

    public void setupPlayersOperations() {
        operationAPI.getPlayerOperationMap().clear();

        if (operationAPI.getMedalsCFG().getConfigurationSection("Accounts") == null) return;

        for (String user : operationAPI.getMedalsCFG().getConfigurationSection("Accounts")) {

            List<Operation> operations = new ArrayList<>();
            Operation using = operationManager.getOperationByName(operationAPI.getMedalsCFG().getString("Accounts." + user + ".Using"));

            String medals = operationAPI.getMedalsCFG().getString("Accounts." + user + ".Medals");

            if (medals.contains(", ")) {

                for (String name : medals.split(", ")) {
                    Operation operation = operationManager.getOperationByName(name);

                    if (operation != null) operations.add(operation);
                }

            } else {
                Operation operation = operationManager.getOperationByName(medals);

                if (operation != null) operations.add(operation);
            }

            PlayerOperation playerOperation = new PlayerOperation(user);

            playerOperation.setCurrent(using);
            playerOperation.setOperations(operations);

            operationAPI.getPlayerOperationMap().put(user.toLowerCase(), playerOperation);
        }
    }

    public void loadPlayersOperations() {

        if (operationAPI.getPlayerOperationsCFG().getConfigurationSection("Accounts") != null) {

            for (String user : operationAPI.getPlayerOperationsCFG().getConfigurationSection("Accounts")) {
                user = user.toLowerCase();

                List<ProgressOperation> progressOperations = new ArrayList<>();

                for (Operation operation : operationManager.getOperations()) {
                    Map<Operation.Type, Long> progress = new HashMap<>();
                    List<Operation.Type> preFinished = new ArrayList<>();

                    String operationName = operation.getName().toLowerCase();

                    String path = "Accounts." + user + "." + operationName;

                    if (operationAPI.getPlayerOperationsCFG().getStringList(path + ".Progress") != null) {

                        for (String value : operationAPI.getPlayerOperationsCFG().getStringList(path + ".Progress")) {
                            Operation.Type type = Operation.Type.valueOf(value.split(", ")[0].toUpperCase());
                            long amount = Long.parseLong(value.split(", ")[1]);

                            progress.put(type, amount);
                        }
                    }

                    if (operationAPI.getPlayerOperationsCFG().getStringList(path + ".Pre-Finished") != null) {
                        for (String value : operationAPI.getPlayerOperationsCFG().getStringList(path + ".Pre-Finished")) {
                            preFinished.add(Operation.Type.valueOf(value.toUpperCase()));
                        }
                    }

                    ProgressOperation progressOperation = new ProgressOperation(operation);
                    progressOperation.setProgress(progress);
                    progressOperation.setPreFinished(preFinished);

                    progressOperations.add(progressOperation);
                }

                PlayerOperation playerOperation = getPlayerOperation(user);

                if (playerOperation == null) continue;

                List<Operation> toRemoveList = new ArrayList<>();
                for (ProgressOperation progressOperation : progressOperations) {
                    if (!playerOperation.getOperations().contains(progressOperation.getOperation()))
                        toRemoveList.add(progressOperation.getOperation());
                }

                for (Operation operation : toRemoveList) {
                    progressOperations.removeIf(progressOperation -> progressOperation.getOperation().getName().equalsIgnoreCase(operation.getName()));
                }

                playerOperation.setProgressOperations(progressOperations);
            }
        }

        List<Operation> finisheds = new ArrayList<>();
        for (String user : operationAPI.getMedalsCFG().getConfigurationSection("Accounts")) {
            user = user.toLowerCase();

            String finished = operationAPI.getMedalsCFG().getString("Accounts." + user + ".Finisheds");

            if (finished == null) continue;

            if (finished.contains(", ")) {

                for (String name : finished.split(", ")) {
                    Operation operation = operationManager.getOperationByName(name);

                    if (operation != null) finisheds.add(operation);
                }

            } else {
                Operation operation = operationManager.getOperationByName(finished);

                if (operation != null) finisheds.add(operation);
            }

            PlayerOperation playerOperation = getPlayerOperation(user);

            if (playerOperation == null) continue;

            playerOperation.setFinished(finisheds);
        }
    }
}