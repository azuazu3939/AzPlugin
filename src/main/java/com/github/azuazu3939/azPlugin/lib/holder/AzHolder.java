package com.github.azuazu3939.azPlugin.lib.holder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AzHolder extends InventoryHolder {

    @NotNull
    NonNullList<ItemStack> itemList();

    @Nullable
    ItemStack item();
}
