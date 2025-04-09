package com.github.azuazu3939.azPlugin.gimmick.holder;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyAzHolder extends AbstractAzHolder {

    public EmptyAzHolder(int row, String name) {
        super(row, name);
    }

    @NotNull
    @Override
    public NonNullList<ItemStack> itemList() {
        return NonNullList.create();
    }

    @Nullable
    @Override
    public ItemStack item() {
        return null;
    }
}
