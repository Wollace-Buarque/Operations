package dev.cromo29.operations.objects;

import dev.cromo29.durkcore.SpecificUtils.NumberUtil;
import dev.cromo29.durkcore.Util.MakeItem;
import dev.cromo29.durkcore.Util.TXT;
import dev.cromo29.operations.OperationPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Treasure {

    private static final OperationPlugin PLUGIN = OperationPlugin.get();

    private final Operation operation;
    private final int level;
    private final List<ItemStack> items;
    private final double percentage;

    private double minPercentage, maxPercentage;

    public Treasure(Operation operation, int level, double percentage, List<ItemStack> items) {
        this.operation = operation;
        this.level = level;
        this.items = items;
        this.percentage = percentage;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getLevel() {
        return level;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public double getPercentage() {
        return percentage;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(double minPercentage) {
        this.minPercentage = minPercentage;
    }

    public double getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(double maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    public ItemStack getBook() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        FileConfiguration configuration = PLUGIN.getConfig();

        String x = configuration.getString("Settings.Treasure.X").trim();
        String z = configuration.getString("Settings.Treasure.Z").trim();

        int xMin = NumberUtil.getInt(x.split(",")[0]);
        int xMax = NumberUtil.getInt(x.split(",")[1]);

        int y = configuration.getInt("Settings.Treasure.Y");

        int zMin = NumberUtil.getInt(z.split(",")[0]);
        int zMax = NumberUtil.getInt(z.split(",")[1]);

        String name = configuration.getString("Settings.Treasure.Name");

        ItemStack book = new MakeItem(Material.WRITTEN_BOOK)
                .setName(name)
                .addLoreList(
                        "",
                        " <7>Operação: " + operation.getName(),
                        " <7>Nível: " + level,
                        " <7>X: " + random.nextInt(xMin, xMax),
                        " <7>Y: " + y,
                        " <7>Z: " + random.nextInt(zMin, zMax),
                        "",
                        "  <e>Digite /caçar e encontre as coordenadas. ",
                        "")
                .removeAttributes()
                .build();

        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        String title = configuration.getString("Settings.Treasure.Title");
        String author = configuration.getString("Settings.Treasure.Author");

        bookMeta.setTitle(TXT.parse(title));
        bookMeta.setAuthor(TXT.parse(author));

        book.setItemMeta(bookMeta);

        return book;
    }

}
