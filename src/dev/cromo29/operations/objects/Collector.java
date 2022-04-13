package dev.cromo29.operations.objects;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class Collector {

    private final String user;
    private final List<ItemStack> items;

    public Collector(String user, List<ItemStack> items) {
        this.user = user;
        this.items = items;
    }

    public String getUser() {
        return user;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void addItems(ItemStack... itemStacks) {
        items.addAll(Arrays.asList(itemStacks));
    }
}
