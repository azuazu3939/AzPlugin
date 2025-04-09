package com.github.azuazu3939.azPlugin.gimmick;

import com.github.azuazu3939.azPlugin.gimmick.holder.AbstractAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.holder.BaseAzHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShowCaseBuilder {

    public static final class Data {

        private static int overAllCount = 0;
        private final int count;
        public final NonNullList<ItemStack> items;
        public final ItemStack cursor;

        public Data(NonNullList<ItemStack> items, ItemStack cursor) {
            this.items = items;
            this.cursor = cursor;
            overAllCount++;
            count = overAllCount;
        }

        public int count() {return count;}

        public NonNullList<ItemStack> items() {return items;}

        public ItemStack cursor() {return cursor;}

        @Override
        public boolean equals(@Nullable Object o) {
            if (!(o instanceof Data data)) return false;
            return data.count() == this.count;
        }
    }

    private static final Map<UUID, Data> TEMP = new ConcurrentHashMap<>();

    public static void create(@NotNull Player player, @NotNull AbstractAzHolder azHolder) {
        Data data = new Data(azHolder.itemList(), azHolder.item());
        TEMP.put(player.getUniqueId(), data);
        player.closeInventory();
        player.openInventory(azHolder.getInventory());
    }

    @NotNull
    @Contract("_ -> new")
    public static Data get(UUID uuid) {return (TEMP.containsKey(uuid)) ? TEMP.get(uuid) : new Data(NonNullList.create(), getEmpty());}

    public static void remove(UUID uuid) {TEMP.remove(uuid);}

    @NotNull
    public static ItemStack getEmpty() {return ItemStack.fromBukkitCopy(null);}

    public static boolean checkHolder(@NotNull Inventory inv) {
        return inv.getHolder() instanceof BaseAzHolder;
    }
}
