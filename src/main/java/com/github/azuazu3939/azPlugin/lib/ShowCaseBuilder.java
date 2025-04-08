package com.github.azuazu3939.azPlugin.lib;

import net.kyori.adventure.text.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShowCaseBuilder implements InventoryHolder {

    public record Data(NonNullList<ItemStack> items, ItemStack cursor) {}

    private final Inventory inv;

    private static final Map<UUID, Data> TEMP = new ConcurrentHashMap<>();

    public ShowCaseBuilder(@NotNull Player player, int rowSize, String name, @NotNull List<org.bukkit.inventory.ItemStack> items, @Nullable org.bukkit.inventory.ItemStack cursor) {
        this.inv = Bukkit.createInventory(this, Math.min(6, Math.max(1, rowSize)), Component.text(name));

        NonNullList<ItemStack> newItems = NonNullList.create();
        for (org.bukkit.inventory.ItemStack item : items) {
            newItems.add(ItemStack.fromBukkitCopy(item));
        }
        TEMP.put(player.getUniqueId(), new Data(newItems, ItemStack.fromBukkitCopy(cursor)));

        player.closeInventory();
        player.openInventory(inv);
    }

    public ShowCaseBuilder(Player player, int rowSize, String name, @NotNull Inventory inventory, @Nullable org.bukkit.inventory.ItemStack cursor) {
        this.inv = Bukkit.createInventory(this, Math.min(6, Math.max(1, rowSize)), Component.text(name));

        NonNullList<ItemStack> newItems = NonNullList.create();
        for (org.bukkit.inventory.ItemStack item : inventory.getContents()) {
            newItems.add(ItemStack.fromBukkitCopy(item));
        }
        TEMP.put(player.getUniqueId(), new Data(newItems, ItemStack.fromBukkitCopy(cursor)));

        player.closeInventory();
        player.openInventory(inv);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }

    @NotNull
    @Contract("_ -> new")
    public static Data get(UUID uuid) {
        return (TEMP.containsKey(uuid)) ? TEMP.get(uuid) : new Data(NonNullList.create(), ItemStack.EMPTY);
    }

    public static void remove(UUID uuid) {
        TEMP.remove(uuid);
    }

    @NotNull
    public static ItemStack getEmpty() {
        return ItemStack.fromBukkitCopy( new org.bukkit.inventory.ItemStack(Material.AIR));
    }

    public static boolean checkHolder(@NotNull Inventory inv) {
        return inv.getHolder() instanceof ShowCaseBuilder;
    }
}
