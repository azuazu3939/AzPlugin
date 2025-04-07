package com.github.azuazu3939.azPlugin.lib;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class AzHolder implements InventoryHolder {

    private final int raw;
    private final String name;

    public AzHolder(int raw, String name) {
        this.raw = Math.min(Math.max(1, raw), 6);
        this.name = name;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, 9 * raw, Component.text(name));
    }
}
