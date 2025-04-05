package com.github.azuazu3939.azPlugin.lib.conditions;

import org.bukkit.inventory.ItemStack;

import java.util.Random;

public record ActionItemStack(ItemStack item, double chance, long tick, DataValue conditions) implements ConditionsRecord {

    public boolean check() {
        return new Random().nextDouble() < chance && conditions().check();
    }
}
