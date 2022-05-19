package dev.cromo29.operations.objects;

import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.OperationPlugin;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Operation {
    private final String name, display, tag;
    private final ItemStack icon;
    private final List<String> lore;
    private final Map<Type, Long> typeMap;
    private final double value;
    private boolean enabled;

    public Operation(String name, String tag, String display, ItemStack icon, double value, boolean enabled, List<String> lore, Map<Type, Long> typeMap) {
        this.name = name;
        this.tag = tag;
        this.display = display;
        this.icon = icon;
        this.value = value;
        this.enabled = enabled;
        this.lore = lore;
        this.typeMap = typeMap;
    }

    public String getName() {
        return name;
    }

    public String getDisplay() {
        return display;
    }

    public String getTag() {
        return tag;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public double getValue() {
        return value;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<Type, Long> getTypes() {
        return typeMap;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getCustomLore() {
        List<String> lore = new ArrayList<>();
        String status = enabled ? "ativada" : "desativada";

        for (String text : OperationPlugin.get().getOperationAPI().getOperationsCFG().getStringList("Operations." + name + ".Info lore")) {
            lore.add(TXT.parse(text.replace("{status}", status)
                    .replace("{display}", display)
                    .replace("{tag}", tag)));
        }

        return lore;
    }

    public enum Type {

        KILL_PLAYER("Matar jogadores"),
        KILL_IN_GLAD("Matar no gladiador"),
        KILL_IN_X1("Matar no x1"),
        KILL_IN_ARENA("Matar na arena"),

        BREAK_COAL("Quebrar carvão"),
        BREAK_LAPIS("Quebrar lapis"),
        BREAK_REDSTONE("Quebrar redstone"),
        BREAK_IRON("Quebrar ferro"),
        BREAK_GOLD("Quebrar ouro"),
        BREAK_EMERALD("Quebrar esmeralda"),
        BREAK_DIAMOND("Quebrar diamante");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getReplacedName() {
            String replacedName;

            switch (name) {
                case "Matar no gladiador":
                    replacedName = "KILL_IN_GLAD";
                    break;
                case "Matar no x1":
                    replacedName = "KILL_IN_X1";
                    break;
                case "Matar na arena":
                    replacedName = "KILL_IN_ARENA";
                    break;
                default:
                    replacedName = "KILL_PLAYER";
                    break;
                case "Quebrar carvão":
                    replacedName = "BREAK_COAL";
                    break;
                case "Quebrar lapis":
                    replacedName = "BREAK_LAPIS";
                    break;
                case "Quebrar redstone":
                    replacedName = "BREAK_REDSTONE";
                    break;
                case "Quebrar ferro":
                    replacedName = "BREAK_IRON";
                    break;
                case "Quebrar ouro":
                    replacedName = "BREAK_GOLD";
                    break;
                case "Quebrar esmeralda":
                    replacedName = "BREAK_EMERALD";
                    break;
                case "Quebrar diamante":
                    replacedName = "BREAK_DIAMOND";
                    break;
            }

            return replacedName;
        }
    }
}