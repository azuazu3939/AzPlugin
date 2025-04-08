package com.github.azuazu3939.azPlugin.lib.packet;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record BlockInteractAction(String key, Inventory inv, ItemStack cursor) {
}
