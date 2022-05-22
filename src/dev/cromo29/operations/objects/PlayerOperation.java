package dev.cromo29.operations.objects;

import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.durkcore.util.VaultAPI;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.api.OperationAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerOperation {

    private static final OperationPlugin PLUGIN = OperationPlugin.get();

    private final String user;
    private String tag;
    private Operation current;
    private List<Operation> operations, finisheds;
    private List<ProgressOperation> progressOperations;

    public PlayerOperation(String user) {
        this.user = user;
        this.operations = new ArrayList<>();
        this.progressOperations = new ArrayList<>();
        this.finisheds = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public Operation getCurrent() {
        return current;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public List<Operation> getFinisheds() {
        return finisheds;
    }

    public List<ProgressOperation> getProgressOperations() {
        return progressOperations;
    }

    public void setProgressOperations(List<ProgressOperation> progressOperations) {
        this.progressOperations = progressOperations;
    }

    public void setFinisheds(List<Operation> finisheds) {
        this.finisheds = finisheds;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;

        for (Operation operation : operations) {
            if (finisheds.contains(operation)) continue;

            final ProgressOperation progressOperation = new ProgressOperation(operation);

            for (Operation.Type type : operation.getTypes().keySet()) {
                progressOperation.getProgress().put(type, 0L);
            }

            progressOperations.add(progressOperation);
        }
    }

    public void setCurrent(Operation operation) {
        this.current = operation;
    }

    public int getCompleteds() {
        int completed = 0;

        Operation operation = getCurrent();

        for (Operation.Type checkType : operation.getTypes().keySet()) {

            for (ProgressOperation progressOperation : progressOperations) {
                if (!progressOperation.getOperation().getName().equalsIgnoreCase(operation.getName())) continue;

                if (progressOperation.getPreFinished().contains(checkType)) completed++;
            }

        }

        return completed;
    }

    public boolean hasFinished(Operation operation) {
        int remaining = 0;

        if (finisheds.contains(operation)) return true;

        for (ProgressOperation progressOperation : progressOperations) {
            if (!progressOperation.getOperation().getName().equalsIgnoreCase(operation.getName())) continue;

            for (Operation.Type type : operation.getTypes().keySet()) {

                if (!progressOperation.getPreFinished().contains(type)) {
                    remaining++;
                }

            }
        }

        return remaining == 0;
    }

    public boolean hasOperation(Operation operation) {
        return operations.contains(operation);
    }

    public boolean hasMoney(Player player, Operation operation) {

        double value = operation.getValue();

        // Desconto
        if (player.hasPermission("29Operations.VIP")) value -= PLUGIN.getConfig().getLong("Settings.Discount");
        if (value < 0) value = 0;

        return VaultAPI.getEconomy().has(player, value);
    }

    public boolean corretlyWorld(Player player) {
        for (String world : PLUGIN.getConfig().getStringList("Settings.Break worlds")) {
            if (player.getWorld().getName().equalsIgnoreCase(world)) return true;
        }

        return false;
    }

    public void addOperation(Operation operation) {
        if (!hasOperation(operation)) operations.add(operation);

        final OperationAPI operationAPI = PLUGIN.getOperationAPI();
        final ProgressOperation progressOperation = new ProgressOperation(operation);

        for (Operation.Type type : operation.getTypes().keySet()) {
            progressOperation.getProgress().put(type, 0L);
        }

        progressOperations.add(progressOperation);

        final String operationsString = TXT.createString(operations.stream().map(Operation::getName).toArray(), 0, ", ");

        operationAPI.getMedalsCFG().set("Accounts." + user.toLowerCase() + ".Medals", operationsString);
        operationAPI.getMedalsCFG().save();
    }
}
