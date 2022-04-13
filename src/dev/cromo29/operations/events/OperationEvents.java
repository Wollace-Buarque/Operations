package dev.cromo29.operations.events;

import dev.cromo29.chat.api.JsonTag;
import dev.cromo29.chat.api.events.PublicMessageEvent;
import dev.cromo29.durkcore.SpecificUtils.LocationUtil;
import dev.cromo29.durkcore.SpecificUtils.PlayerUtil;
import dev.cromo29.durkcore.Util.*;
import dev.cromo29.operations.api.OperationAPI;
import dev.cromo29.operations.managers.CollectorManager;
import dev.cromo29.operations.managers.OperationManager;
import dev.cromo29.operations.OperationPlugin;
import dev.cromo29.operations.managers.PlayerOperationManager;
import dev.cromo29.operations.managers.TreasureManager;
import dev.cromo29.operations.objects.Operation;
import dev.cromo29.operations.objects.PlayerOperation;
import dev.cromo29.operations.objects.ProgressOperation;
import dev.cromo29.operations.objects.Treasure;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationEvents implements Listener {

    private final OperationPlugin plugin;

    private final OperationAPI operationAPI;

    private final OperationManager operationManager;
    private final PlayerOperationManager playerOperationManager;
    private final TreasureManager treasureManager;
    private final CollectorManager collectorManager;

    private final Map<String, Integer> taskMap;

    public OperationEvents(OperationPlugin plugin) {
        this.plugin = plugin;

        this.operationAPI = plugin.getOperationAPI();

        this.operationManager = operationAPI.getOperationManager();
        this.playerOperationManager = operationAPI.getPlayerOperationManager();
        this.treasureManager = operationAPI.getTreasureManager();
        this.collectorManager = operationAPI.getCollectorManager();

        this.taskMap = new HashMap<>();
    }

    @EventHandler
    public void chatEvent(PublicMessageEvent event) {
        final Player player = event.getSender();
        final PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

        if (playerOperation == null
                || playerOperation.getTag() == null
                || playerOperation.getTag().equalsIgnoreCase("Nenhuma"))
            return;

        JsonTag jsonTag = new JsonTag(TXT.parse(playerOperation.getTag()));

        jsonTag.setTooltip(playerOperation.getCurrent().getCustomLore());

        event.setTagValue("29Operations", jsonTag);
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

        if (playerOperation == null) {
            playerOperation = new PlayerOperation(player.getName());

            operationAPI.getPlayerOperationMap().put(player.getName().toLowerCase(), playerOperation);
        }

        if (playerOperation.getCurrent() == null && !playerOperation.getOperations().isEmpty())
            playerOperation.setCurrent(playerOperation.getOperations().get(0));
    }

    @EventHandler
    public void activateOperation(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = player.getItemInHand();

        if (item == null
                || item.getType() != Material.PAPER
                || !item.hasItemMeta()
                || !item.getItemMeta().hasDisplayName()
                || !item.getItemMeta().hasLore())
            return;

        String name = item.getItemMeta().getDisplayName();
        for (String text : item.getItemMeta().getLore()) {
            if (text.contains("Operação: ")) name = text;
        }

        name = name.split("Operação: ")[1];

        final Operation operation = operationManager.getOperationByName(ChatColor.stripColor(name.trim()));

        if (operation == null) return;

        final PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

        if (playerOperation == null) {
            sendMessage(player,
                    "",
                    " <c>Ocorreu um erro ao ativar esta operação!",
                    " <c>Reentre no servidor e tente novamente!",
                    " <c>Caso o erro continue, contate um STAFF!",
                    "");

            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            return;
        }

        if (playerOperation.hasOperation(operation)) {
            sendMessage(player,
                    "",
                    " <c>Você já possui a operação <f>" + operation.getDisplay() + "<c>!",
                    " <c>Não se preocupe! Você pode vende-la para alguém.",
                    "");

            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
            return;
        }

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else player.setItemInHand(null);

        player.updateInventory();

        playerOperation.addOperation(operation);
        playerOperation.setCurrent(operation);

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

        sendGMessage("",
                " <b>" + player.getName() + " <e>ativou a operação <b>" + operation.getDisplay() + "<e>!",
                "");

        sendMessage(player, "",
                " <e>Para olhar suas operações digite <b>/operações<e>!",
                " <e>Você também verá os objetivos da operação!",
                "");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakEvent(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (event.isCancelled()) return;

        final PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(player.getName());

        if (playerOperation == null
                || playerOperation.getCurrent() == null
                || !playerOperation.corretlyWorld(player)) return;

        final Operation operation = playerOperation.getCurrent();

        if (playerOperation.hasFinished(operation) || !operation.isEnabled()) return;

        switch (block.getType()) {
            case COAL_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_COAL, block, "+1 bloco de carvão quebrado! <b>{current}<7>/<b>{max}");
                break;
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_REDSTONE, block, "+1 bloco de redstone quebrado! <b>{current}<7>/<b>{max}");
                break;
            case LAPIS_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_LAPIS, block, "+1 bloco de lapis quebrado! <b>{current}<7>/<b>{max}");
                break;
            case IRON_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_IRON, block, "+1 bloco de ferro quebrado! <b>{current}<7>/<b>{max}");
                break;
            case GOLD_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_GOLD, block, "+1 bloco de ouro quebrado! <b>{current}<7>/<b>{max}");
                break;
            case EMERALD_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_EMERALD, block, "+1 bloco de esmeralda quebrado! <b>{current}<7>/<b>{max}");
                break;
            case DIAMOND_ORE:
                addOperationPoint(player, playerOperation, Operation.Type.BREAK_DIAMOND, block, "+1 bloco de diamante quebrado! <b>{current}<7>/<b>{max}");
                break;
        }
    }

    @EventHandler
    public void deathEvent(PlayerDeathEvent event) {

        if (event.getEntity().getKiller() == null) return;

        final Player victim = event.getEntity();
        final Player killer = event.getEntity().getKiller();

        final PlayerOperation playerOperation = playerOperationManager.getPlayerOperation(killer.getName());

        if (playerOperation == null || playerOperation.getCurrent() == null) return;

        final Operation operation = playerOperation.getCurrent();

        if (playerOperation.hasFinished(operation) || !operation.isEnabled()) return;

        if (victim.getAddress().getAddress() == killer.getAddress().getAddress()) {
            sendMessage(killer, "<c>Você não ganhará pontos por matar jogadores com o mesmo IP!");
            return;
        }

            /*if (ArenaAPI.isInArena(killer.getName())) {
                if (playerOperation.getUsing().getTypes().containsKey(Operation.Types.KILL_IN_X1))
                    addOperationPoint(killer, playerOperation, Operation.Types.KILL_IN_X1, null, "+1 abate no x1! <b>{current}<7>/<b>{max}");
                return;
            }

            if (br.trcraft.gladiador.Main.pl.participantes.contains(killer.getName())) {
                if (playerOperation.getUsing().getTypes().containsKey(Operation.Types.KILL_IN_GLAD))
                    addOperationPoint(killer, playerOperation, Operation.Types.KILL_IN_GLAD, null, "+1 abate no gladiador! <b>{current}<7>/<b>{max}");
                return;
            }

            if (ArenaKitPvP.Kitpvp.contains(killer.getName())) {
                if (playerOperation.getUsing().getTypes().containsKey(Operation.Types.KILL_IN_ARENA))
                    addOperationPoint(killer, playerOperation, Operation.Types.KILL_IN_ARENA, null, "+1 abate na arena Kit-PvP! <b>{current}<7>/<b>{max}");
                return;
            }*/

        addOperationPoint(killer, playerOperation, Operation.Type.KILL_PLAYER, null, "+1 abate! <b>{current}<7>/<b>{max}");
    }


    private void addOperationPoint(Player player, PlayerOperation playerOperation, Operation.Type type, Block block, String message) {

        if (playerOperation.getCurrent() == null) return;

        final Operation operation = playerOperation.getCurrent();

        if (!operation.getTypes().containsKey(type) || playerOperation.getFinished().contains(operation)) return;

        final ProgressOperation progressOperation = playerOperationManager.getProgressOperation(playerOperation, operation.getName());

        if (progressOperation == null || progressOperation.getPreFinished().contains(type)) return;

        final long progress = progressOperation.getProgress().getOrDefault(type, 0L) + 1;

        progressOperation.incrementProgress(type, progress);

        if (progressOperation.getProgress().get(type) >= operation.getTypes().get(type)) {
            progressOperation.addPreFinished(type);

            if (block != null) sendEffect(block.getLocation(), type);

            if (playerOperation.hasFinished(operation)) {
                finishOperation(player, operation, playerOperation);
                return;
            }

            int completed = playerOperation.getCompleteds();
            String step = completed + " <a>" + (completed == 1 ? "fase" : "fases");

            sendMessage(player,
                    "",
                    " <a>Você concluiu <f>" + step + " da operação <f>" + operation.getDisplay() + "<a>!",
                    " <a>Fase concluida: <f>" + type.getName() + "<a>.",
                    "");

            PlayerUtil.sendActionBar(player, "<a>Você concluiu <f>" + step + " da operação <f>" + operation.getDisplay() + "<a>!");

            player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);

            giveReward(player, operation, false);
            return;
        }

        message = message
                .replace("{current}", "" + progress)
                .replace("{max}", "" + operation.getTypes().get(type));

        PlayerUtil.sendActionBar(player, message);

        if (block != null) {
            if (progress % 30 == 0) sendMessage(player, message);

            sendEffect(block.getLocation(), type);
            return;
        }

        sendMessage(player, message);
    }

    private void sendEffect(Location location, Operation.Type type) {
        Location loc = location.clone().add(0.5, 0.8, 0.5);

        switch (type) {
            case BREAK_COAL:
                sendEffect(loc, Color.BLACK);
                break;
            case BREAK_REDSTONE:
                sendEffect(loc, Color.RED);
                break;
            case BREAK_LAPIS:
                sendEffect(loc, Color.BLUE);
                break;
            case BREAK_IRON:
                sendEffect(loc, Color.SILVER);
                break;
            case BREAK_GOLD:
                sendEffect(loc, Color.YELLOW);
                break;
            case BREAK_EMERALD:
                sendEffect(loc, Color.fromBGR(73, 235, 52));
                break;
            case BREAK_DIAMOND:
                sendEffect(loc, Color.AQUA);
                break;
        }
    }

    private void giveReward(Player player, Operation operation, boolean ended) {
        FileConfiguration configuration = plugin.getConfig();

        final int minDefault = configuration.getInt("Settings.Treasure.Default.Min");
        final int maxDefault = configuration.getInt("Settings.Treasure.Default.Max");

        final int minEnd = configuration.getInt("Settings.Treasure.End.Min");
        final int maxEnd = configuration.getInt("Settings.Treasure.End.Max");

        final Treasure treasure = treasureManager.getRandomTreasure(operation.getName(),
                ended ? minEnd : minDefault,
                ended ? maxEnd : maxDefault);

        if (treasure == null) {
            sendMessage(player, "", " <c>O seu tesouro não foi encontrado!", "");
            return;
        }

        collectorManager.sendCollector(player, treasure.getBook());

        sendGMessage("",
                " <b>" + player.getName() + " <e>ganhou um tesouro nível <b>" + treasure.getLevel() + "<e> na operação <b>" + operation.getDisplay() + "<e>!",
                "");
    }

    private void finishOperation(Player player, Operation operation, PlayerOperation playerOperation) {

        sendGMessage(
                "",
                " <b>" + player.getName() + " <e>concluiu a operação <b>" + operation.getDisplay() + "<e>!",
                "");

        final ProgressOperation progressOperation = playerOperationManager.getProgressOperation(playerOperation, operation.getName());

        if (progressOperation != null) {
            progressOperation.getPreFinished().clear();
            progressOperation.getProgress().clear();
        }

        String playerName = player.getName().toLowerCase();
        String operationName = operation.getName().toLowerCase();

        ConfigManager playerOperationsCFG = operationAPI.getPlayerOperationsCFG();

        if (playerOperationsCFG.contains("Accounts." + playerName + "." + operationName)) {
            TXT.runAsynchronously(plugin, () -> {

                boolean everything = (long) playerOperationsCFG.getConfigurationSection("Accounts." + playerName)
                        .size() <= 1;

                if (everything) playerOperationsCFG.set("Accounts." + playerName, null);
                else playerOperationsCFG.set("Accounts." + playerName + "." + operationName, null);

                playerOperationsCFG.save();
            });
        }

        playerOperation.getFinished().add(operation);

        giveReward(player, operation, true);
        sendAnimatedText(player, operation);

        PlayerUtil.sendActionBar(player, "<a>Você concluiu a operação <f>" + operation.getDisplay() + "<a>!");
    }

    private void sendAnimatedText(Player player, Operation operation) {
        if (taskMap.containsKey(player.getName())) {
            plugin.getServer().getScheduler().cancelTask(taskMap.get(player.getName()));

            taskMap.remove(player.getName());
        }

        final TextAnimation animation = new TextAnimation(new TextAnimation.Hypixel(
                TXT.parse("Você concluiu a operação " + operation.getName() + "!"),
                "<f>",
                "<l>",
                "<f>",
                "<b><l>",
                1,
                3,
                "<f>",
                "<f><l>",
                40));

        final AtomicInteger timer = new AtomicInteger();

        taskMap.put(player.getName(), plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            timer.getAndIncrement();

            PlayerUtil.sendActionBar(player, animation.next());
            LocationUtil.detonateRandomFirework(player.getLocation());

            if (timer.get() < 45 || !taskMap.containsKey(player.getName())) return;

            plugin.getServer().getScheduler().cancelTask(taskMap.get(player.getName()));
            taskMap.remove(player.getName());

        }, 0, 3));
    }

    private void sendEffect(Location location, Color color) {
        ParticleMaker.sendParticle(ParticleEffect.REDSTONE, location, color, 25, new ArrayList<>(plugin.getServer().getOnlinePlayers()));
    }

    private void sendGMessage(String... messages) {
        plugin.getServer().getOnlinePlayers().forEach(player -> TXT.sendMessages(player, messages));
    }

    private void sendMessage(Player player, String... messages) {
        TXT.sendMessages(player, messages);
    }
}