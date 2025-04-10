package com.github.azuazu3939.azPlugin.gimmick.records;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record BlockInteractAction(Inventory inv, ItemStack cursor) implements BlockAction {
}
