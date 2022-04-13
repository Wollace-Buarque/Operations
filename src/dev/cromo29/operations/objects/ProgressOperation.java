package dev.cromo29.operations.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressOperation {

    private Operation operation;

    private Map<Operation.Type, Long> progress;
    private List<Operation.Type> preFinished;

    public ProgressOperation(Operation operation) {
        this.operation = operation;
        this.progress = new HashMap<>();
        this.preFinished = new ArrayList<>();
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Map<Operation.Type, Long> getProgress() {
        return progress;
    }

    public void incrementProgress(Operation.Type type, long value) {
        progress.put(type, value);
    }

    public void setProgress(Map<Operation.Type, Long> progress) {
        this.progress = progress;
    }

    public List<Operation.Type> getPreFinished() {
        return preFinished;
    }

    public void addPreFinished(Operation.Type type) {
        preFinished.add(type);
    }

    public void setPreFinished(List<Operation.Type> preFinished) {
        this.preFinished = preFinished;
    }
}
