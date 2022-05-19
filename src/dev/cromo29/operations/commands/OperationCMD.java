package dev.cromo29.operations.commands;

import dev.cromo29.durkcore.API.DurkCommand;
import dev.cromo29.operations.api.OperationAPI;

import java.util.List;

public class OperationCMD extends DurkCommand {

    private final OperationAPI operationAPI;

    public OperationCMD(OperationAPI operationAPI) {
        this.operationAPI = operationAPI;
    }

    @Override
    public void perform() {

        if (!isArgsLength(1)) {
            operationAPI.getGuiManager().showPlayerOperations(asPlayer());
            return;
        }

        if (isArgAtIgnoreCase(0, "mob")) {

            if (!hasPermission("29Operations.*")) {
                operationAPI.getGuiManager().showPlayerOperations(asPlayer(), argAt(0));
                return;
            }

            operationAPI.getLocationsGson().putLocation("Mob", asPlayer().getLocation());
            operationAPI.getLocationsGson().save();

            operationAPI.getOperationManager().spawnMob();

            sendMessage("<a>Você setou o vendedor de operações!");

        } else if (isArgAtIgnoreCase(0, "reload")) {

            if (!hasPermission("29Operations.*")) {
                operationAPI.getGuiManager().showPlayerOperations(asPlayer(), argAt(0));
                return;
            }

            operationAPI.reloadAll();

            sendMessage(" <a>Você recarregou todas configurações com sucesso!");

        } else if (isArgAtIgnoreCase(0, "coletar")) {

            operationAPI.getCollectorManager().openCollector(asPlayer());

        } else if (isArgAtIgnoreCase(0, "tesouro")) {

            if (!hasPermission("29Operations.*")) {
                operationAPI.getGuiManager().showPlayerOperations(asPlayer(), argAt(0));
                return;
            }

            operationAPI.getLocationsGson().putLocation("Treasure", asPlayer().getLocation());
            operationAPI.getLocationsGson().save();

            sendMessage("<a>Você setou o mundo de tesouros!");

        } else if (isArgAtIgnoreCase(0, "comprar")) {

            operationAPI.getGuiManager().showOperations(asPlayer());

        } else if (isArgAtIgnoreCase(0, "?", "help", "ajuda")) {

            if (hasPermission("29Operations.*"))
                sendMessages("",
                        "<b>- <r>/caçar - <7>Ir para o mundo de tesouros.",
                        "<b>- <r>/" + getUsedCommand() + " - <7>Suas próprias operações.",
                        "<b>- <r>/" + getUsedCommand() + " ? - <7>Mostrar essas mensagens.",
                        "<b>- <r>/" + getUsedCommand() + " mob - <7>Setar local do villager.",
                        "<b>- <r>/" + getUsedCommand() + " tesouro - <7>Setar mundo do tesouro.",
                        "<b>- <r>/" + getUsedCommand() + " reload - <7>Recarregar configurações.",
                        "<b>- <r>/" + getUsedCommand() + " comprar - <7>Comprar operações.",
                        "<b>- <r>/" + getUsedCommand() + " coletar - <7>Coletar premios.",
                        "<b>- <r>/" + getUsedCommand() + " <nick> - <7>Operações de alguém.",
                        "");
            else
                sendMessages("",
                        "<b>- <r>/caçar - <7>Ir para o mundo de tesouros.",
                        "<b>- <r>/" + getUsedCommand() + " - <7>Suas próprias operações.",
                        "<b>- <r>/" + getUsedCommand() + " ? - <7>Mostrar essas mensagens.",
                        "<b>- <r>/" + getUsedCommand() + " comprar - <7>Comprar operações.",
                        "<b>- <r>/" + getUsedCommand() + " coletar - <7>Coletar premios.",
                        "<b>- <r>/" + getUsedCommand() + " <nick> - <7>Operações de alguém.",
                        "");

        } else operationAPI.getGuiManager().showPlayerOperations(asPlayer(), argAt(0));
    }

    @Override
    public boolean canConsolePerform() {
        return false;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getCommand() {
        return "operacoes";
    }

    @Override
    public List<String> getAliases() {
        return getList("operações", "operacao", "operação");
    }
}
