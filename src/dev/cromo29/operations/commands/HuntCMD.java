package dev.cromo29.operations.commands;

import dev.cromo29.durkcore.api.DurkCommand;
import dev.cromo29.durkcore.entity.DurkPlayer;
import dev.cromo29.operations.OperationPlugin;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

public class HuntCMD extends DurkCommand {

    private final OperationPlugin plugin;

    public HuntCMD(OperationPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void perform() {
        final DurkPlayer durkPlayer = DurkPlayer.of(asPlayer());

        if (!durkPlayer.containsItem(Material.WRITTEN_BOOK, plugin.getConfig().getString("Settings.Treasure.Name"), true)) {
            sendMessage(asPlayer(), " <c>Você precisa ter um livro dos tesouros!");
            return;
        }

        final Location location = plugin.getOperationAPI().getLocationsGson().get("Treasure").asLocation();

        if (location == null) {
            sendMessage("<c>O mundo de tesouros não foi setado!");
            return;
        }

        asPlayer().teleport(location);

        sendMessage(" <b>Você foi teleportado para o mundo de tesouros!");
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
        return "cacar";
    }

    @Override
    public List<String> getAliases() {
        return getList("caçar");
    }
}
