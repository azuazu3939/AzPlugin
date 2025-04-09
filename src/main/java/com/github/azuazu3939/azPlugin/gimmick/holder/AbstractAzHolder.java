package com.github.azuazu3939.azPlugin.gimmick.holder;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractAzHolder implements AzHolder {

    private final Inventory inv;

    public AbstractAzHolder(int row, String name) {
        this.inv = Bukkit.createInventory(this, Math.min(6, Math.max(1, row)) * 9, Component.text(name));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
