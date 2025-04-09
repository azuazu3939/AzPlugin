package com.github.azuazu3939.azPlugin.gimmick.holder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseAzHolder extends AbstractAzHolder {

    private final NonNullList<ItemStack> items;
    private final ItemStack cursor;

    public BaseAzHolder(int row, String name, @NotNull Inventory inv, org.bukkit.inventory.ItemStack cursor) {
        super(row, name);

        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < inv.getSize(); i++) {
            items.add(ItemStack.fromBukkitCopy(inv.getItem(i)));
        }
        this.items = items;
        this.cursor = ItemStack.fromBukkitCopy(cursor);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> itemList() {
        return items;
    }

    @Nullable
    @Override
    public ItemStack item() {
        return cursor;
    }
}
