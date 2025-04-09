package com.github.azuazu3939.azPlugin.lib.packet;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record BlockInteractAction(Inventory inv, ItemStack cursor) {
}
