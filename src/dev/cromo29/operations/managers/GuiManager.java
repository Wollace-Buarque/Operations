package dev.cromo29.operations.managers;

import dev.cromo29.durkcore.inventory.Confirmation;
import dev.cromo29.durkcore.inventory.Inv;
import dev.cromo29.durkcore.specificutils.ListUtil;
import dev.cromo29.durkcore.specificutils.NumberUtil;
import dev.cromo29.durkcore.specificutils.PlayerUtil;
import dev.cromo29.durkcore.util.MakeItem;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.durkcore.util.VaultAPI;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.objects.PlayerOperation;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.objects.ProgressOperation;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GuiManager {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private final PlayerOperationManager playerOperationManager;

    public GuiManager(OperationAPI operationAPI, PlayerOperationManager playerOperationManager) {
        this.operationAPI = operationAPI;
        this.plugin = operationAPI.getPlugin();

        this.playerOperationManager = playerOperationManager;
    }

    public void showOperations(Player player) {
        List<Operation> operations = operationAPI.getOperationManager().getOperations();

        if (operations.isEmpty()) {
            sendMessage(player, " <c>Nenhuma operação no momento!");
            return;
        }

        int enabledOperationsSize = 0;
        for (Operation operation : operations) {

            if (operation.isEnabled()) enabledOperationsSize++;
        }

        if (enabledOperationsSize == 0 && !player.hasPermission("29Operations.*")) {
            sendMessage(player, " <c>Nenhuma operação ativada no momento!");
            return;
        }

        enabledOperationsSize = player.hasPermission("29Operations.*") ? operations.size() : enabledOperationsSize;

        int inventorySize = Math.min(27 + ((enabledOperationsSize / 7) * 9), 54);

        Inv inv = new Inv(inventorySize, "Operações do servidor:");
        inv.setIgnorePlayerInventoryClick(true, true);

        // Desconto
        double discount = player.hasPermission("29Operations.VIP") ?
                plugin.getConfig().getDouble("Settings.Discount")
                :
                0;

        List<String> lore = new ArrayList<>();
        for (Operation operation : operations) {

            lore.clear();
            for (String text : operation.getLore()) {

                if (text.contains(operation.getValue() + "")) {
                    double discountPercentage = (discount / operation.getValue()) * 100;

                    double result = operation.getValue() - discount;

                    if (result < 0) {
                        result = 0;
                        discountPercentage = 100;
                    }

                    text = player.hasPermission("29Operations.VIP") ?
                            text.replace(operation.getValue() + "", result + " &7(" + NumberUtil.formatNumberSimple(discountPercentage) + "% de desconto)")
                            :
                            text.replace(operation.getValue() + "", result + "");
                }

                lore.add(text);
            }

            ItemStack currentItem = new MakeItem(operation.getIcon()).setLore(lore).build();

            inv.setInMiddle(currentItem, event -> {

                PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

                if (player.hasPermission("29Operations.*") && event.getClick() == ClickType.RIGHT) {

                    String title = "<0>Desativar ope. " + operation.getDisplay() + "<0>?";

                    if (!operation.isEnabled()) title = "<0>Ativar ope. " + operation.getDisplay() + "<0>?";

                    Confirmation.confirm(title, operation.getIcon(), player, accept -> {

                        String enabled = operation.isEnabled() ? "desativou" : "ativou";
                        operation.setEnabled(!operation.isEnabled());

                        sendMessage(player, "<a>Você " + enabled + " a operação <f>" + operation.getDisplay() + "<a>!");

                        for (Player user : plugin.getServer().getOnlinePlayers()) {
                            String status = operation.isEnabled() ? "ativada" : "desativada";

                            sendMessage(user, "", " <e>A operação <f>" + operation.getDisplay() + " <e>foi " + status + "!", "");

                            user.playSound(user.getLocation(), Sound.CLICK, 1, 1);
                        }

                        operationAPI.getOperationsCFG().set("Operations." + operation.getName() + ".Enabled", operation.isEnabled());
                        operationAPI.getOperationsCFG().save();

                    }, reject -> {
                        sendMessage(player, "<a>Confirmação cancelada com sucesso!");
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    });
                    return;
                }

                if (playerOperation == null) {
                    sendMessage(player,
                            "",
                            " <c>Ocorreu um erro ao adquirir esta operação!",
                            " <c>Reentre no servidor e tente novamente!",
                            " <c>Caso o erro continue, contate um STAFF!",
                            "");

                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
                    player.closeInventory();
                    return;
                }

                // Desconto
                double value = player.hasPermission("29Operations.VIP") ?
                        operation.getValue() - plugin.getConfig().getDouble("Settings.Discount")
                        :
                        operation.getValue();

                if (value < 0) value = 0;

                if (!playerOperation.hasMoney(player, operation)) {
                    double needed = value - VaultAPI.getEconomy().getBalance(player);

                    sendMessage(player, "",
                            " <c>Você precisa de mais <f>" + NumberUtil.formatNumberSimple(needed) + " coins<c> para comprar,",
                            " <c>a operação <f>" + operation.getDisplay() + "<c>!",
                            "");
                    return;
                }

                if (player.getInventory().firstEmpty() == -1) {
                    sendMessage(player, " <c>Seu inventário está cheio!");
                    return;
                }

                double finalValue = value;
                Confirmation.confirm("Comprar operação " + operation.getName() + "?", currentItem, player, accept -> {

                    List<String> anotherLore = ListUtil.getColorizedStringList(
                            "",
                            " &7Operação: &f" + operation.getName(),
                            " &7Clique com o <f>papel na mão <7>para ativar a operação. ",
                            "");

                    player.getInventory().addItem(new MakeItem(operation.getIcon())
                            .setMaterial(Material.PAPER)
                            .setData(0)
                            .setLore(anotherLore)
                            .build());

                    VaultAPI.getEconomy().withdrawPlayer(player, finalValue);

                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

                    sendMessage(player,
                            "",
                            " <a>Você adquiriu a operação <f>" + operation.getDisplay() + "<a>!",
                            "");

                    PlayerUtil.sendActionBar(player, "<a>Operação <f>" + operation.getDisplay() + " <a>adquirida!");

                    player.closeInventory();
                }, reject -> {
                    sendMessage(player, "",
                            " <c>Você cancelou a compra da operação <f>" + operation.getDisplay() + "<c>!",
                            "");

                    PlayerUtil.sendActionBar(player, "<c>Compra cancelada!");

                    player.closeInventory();
                });

            });
        }

        inv.open(player);
    }

    public void showPlayerOperations(Player player) {
        List<Operation> operations = playerOperationManager.getPlayerOperations(player.getName());
        PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

        if (playerOperation == null) {
            sendMessage(player, "",
                    " <c>Você não possui nenhuma operação!",
                    " <c>Caso realmente possua alguma, tente relogar!",
                    " <c>Se o problema persistir contate um staff.",
                    "");
            return;
        }

        if (operations.isEmpty()) {
            sendMessage(player, " <c>Você não possui nenhuma operação!");
            return;
        }

        Inv inv = new Inv(36, "Suas operações:");
        inv.setIgnorePlayerInventoryClick(true, true);

        for (Operation operation : operations) {
            inv.setInMiddle(new MakeItem(operation.getIcon())
                    .setLore(operation.getCustomLore())
                    .setGlow(playerOperation.getCurrent() == operation)
                    .build(), event -> {

                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);

                showOperationSettings(player, operation, playerOperation);
            });
        }

        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
        inv.open(player);
    }

    public void showPlayerOperations(Player player, String targetName) {
        List<Operation> operations = playerOperationManager.getPlayerOperations(targetName);
        PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(targetName);

        if (player.getName().equalsIgnoreCase(targetName)) {
            showPlayerOperations(player);
            return;
        }

        if (playerOperation == null || operations.isEmpty()) {
            sendMessage(player,
                    "",
                    " " + targetName + " <c>não possui nenhuma operação!",
                    "");
            return;
        }

        Inv inv = new Inv(36, "Operações de " + targetName + ":");
        inv.setIgnorePlayerInventoryClick(true, true);

        for (Operation operation : operations) {
            inv.setInMiddle(new MakeItem(operation.getIcon())
                    .setLore(operation.getCustomLore())
                    .setGlow(playerOperation.getCurrent() == operation)
                    .build());
        }

        inv.open(player);
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1, 1);
    }

    private void showOperationSettings(Player player, Operation operation, PlayerOperation playerOperation) {
        Inv inv = new Inv(27, "Detalhes da operação:");
        inv.setIgnorePlayerInventoryClick(true, true);

        inv.addClickHandler(event -> {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
        });

        inv.setItem(11, new MakeItem(operation.getIcon())
                .setName(" <r> ")
                .setLore(new ArrayList<>())
                .addLoreList(
                        "",
                        " &7Operação &e" + operation.getDisplay() + "&7! ",
                        " &7Tag: &f" + operation.getTag(),
                        "",
                        " &7Clique com o &fbotão esquerdo &7para ativar a tag da operação! ",
                        " &7Clique com o &fbotão direito &7para começar a usar esta operação! ",
                        "")
                .build(), event -> {

            if (event.getClick().isLeftClick()) {

                String tag = operation.getTag();

                if (tag != null && tag.endsWith(" ")) tag = tag.substring(0, tag.length() - 1);

                if (playerOperation.getTag() != null && !playerOperation.getTag().equalsIgnoreCase("Nenhuma")) {
                    if (playerOperation.getTag().equalsIgnoreCase(operation.getTag())) {

                        playerOperation.setTag("Nenhuma");

                        sendMessage(player, "",
                                " <a>Você desativou a tag <f>" + tag + "<a>!",
                                "");

                        player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
                        return;
                    }
                }

                player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);

                playerOperation.setTag(operation.getTag());

                sendMessage(player,
                        "",
                        " <a>Você ativou a tag <f>" + tag + "<a>!",
                        "");

            } else if (event.getClick().isRightClick()) {

                if (playerOperation.getCurrent().getName().equalsIgnoreCase(operation.getName())) {
                    sendMessage(player,
                            "",
                            " <c>Você já está usando a operação <f>" + operation.getDisplay() + "<c>!",
                            "");
                    return;
                }

                playerOperation.setCurrent(operation);

                operationAPI.getMedalsCFG().set("Accounts." + player.getName().toLowerCase() + ".Using", operation.getName());
                operationAPI.getMedalsCFG().save();

                sendMessage(player,
                        "",
                        " <a>Você começou a usar a operação <f>" + operation.getDisplay() + "<a>!",
                        "");

                player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
            }

        });

        List<String> miningLore = new ArrayList<>();
        List<String> headLore = new ArrayList<>();

        // Abates

        headLore.add("");

        if (!updateHeadLore(operation, playerOperation, headLore))
            headLore.add(" &cNenhuma missão de abates nesta operação. ");

        headLore.add("");

        inv.setItem(13, new MakeItem(player.getName()).setName(" <r> ")
                .setLore(headLore)
                .build());

        // Blocos

        miningLore.add("");

        if (!updateMiningLore(operation, playerOperation, miningLore))
            miningLore.add(" &cNenhuma missão de mineração nesta operação. ");

        miningLore.add("");

        inv.setItem(15, new MakeItem(Material.DIAMOND_PICKAXE)
                .setName(" <r> ")
                .setLore(miningLore)
                .build());

        inv.setItem(26, new MakeItem(Material.ARROW)
                        .setName(" <r> ")
                        .addLoreList("", " <e>Clique aqui para voltar.", "")
                        .build(),
                event -> showPlayerOperations(player));

        inv.open(player);
    }

    private String getPercentage(PlayerOperation playerOperation, Operation operation, Operation.Type type, long amount) {
        long current = 0;

        if (playerOperation.getFinisheds().contains(operation)) return "&7(100%)";

        ProgressOperation progressOperation = playerOperationManager.getProgressOperation(playerOperation, operation.getName());

        if (progressOperation == null) return "&7(0%)";

        if (progressOperation.getProgress().containsKey(type)) current = progressOperation.getProgress().get(type);

        String percentage;

        if (progressOperation.getPreFinished().contains(type)) percentage = "&7(100%)";
        else percentage = "&7(" + percent(current, amount) + ")";

        return percentage;
    }

    private String percent(double value, double max) {
        double percent = (value * 100) / max;

        return NumberUtil.formatNumberSimple(percent) + "%";
    }

    void sendMessage(Player player, String... messages) {
        TXT.sendMessages(player, messages);
    }

    // Código ruim

    private boolean updateMiningLore(Operation operation, PlayerOperation playerOperation, List<String> miningLore) {
        boolean mining = false;

        Map<Operation.Type, Long> types = operation.getTypes();

        if (types.containsKey(Operation.Type.BREAK_COAL)) {
            long amount = types.get(Operation.Type.BREAK_COAL);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Carvão! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_COAL, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_REDSTONE)) {
            long amount = types.get(Operation.Type.BREAK_REDSTONE);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Redstone! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_REDSTONE, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_LAPIS)) {
            long amount = types.get(Operation.Type.BREAK_LAPIS);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Lapis! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_LAPIS, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_IRON)) {
            long amount = types.get(Operation.Type.BREAK_IRON);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Ferro! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_IRON, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_GOLD)) {
            long amount = types.get(Operation.Type.BREAK_GOLD);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Ouro! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_GOLD, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_EMERALD)) {
            long amount = types.get(Operation.Type.BREAK_EMERALD);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Esmeralda! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_EMERALD, amount));
        }

        if (types.containsKey(Operation.Type.BREAK_DIAMOND)) {
            long amount = types.get(Operation.Type.BREAK_DIAMOND);
            mining = true;

            miningLore.add(" &eQuebre " + amount + " minérios de Diamante! " + getPercentage(playerOperation, operation, Operation.Type.BREAK_DIAMOND, amount));
        }

        return mining;
    }

    private boolean updateHeadLore(Operation operation, PlayerOperation playerOperation, List<String> headLore) {
        boolean head = false;

        Map<Operation.Type, Long> types = operation.getTypes();

        if (types.containsKey(Operation.Type.KILL_PLAYER)) {
            long amount = types.get(Operation.Type.KILL_PLAYER);
            head = true;

            headLore.add(" &eMate " + amount + " jogadores! " + getPercentage(playerOperation, operation, Operation.Type.KILL_PLAYER, amount));
        }

        if (types.containsKey(Operation.Type.KILL_IN_ARENA)) {
            long amount = types.get(Operation.Type.KILL_IN_ARENA);
            head = true;

            headLore.add(" &eMate " + amount + " jogadores na arena Kit-PvP! " + getPercentage(playerOperation, operation, Operation.Type.KILL_IN_ARENA, amount));
        }

        if (types.containsKey(Operation.Type.KILL_IN_X1)) {
            long amount = types.get(Operation.Type.KILL_IN_X1);
            head = true;

            headLore.add(" &eMate " + amount + " jogadores no X1! " + getPercentage(playerOperation, operation, Operation.Type.KILL_IN_X1, amount));
        }

        if (types.containsKey(Operation.Type.KILL_IN_GLAD)) {
            long amount = types.get(Operation.Type.KILL_IN_GLAD);
            head = true;

            headLore.add(" &eMate " + amount + " jogadores no Gladiador! " + getPercentage(playerOperation, operation, Operation.Type.KILL_IN_GLAD, amount));
        }

        return head;
    }
}
