package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.lib.conditions.ActionBlock;
import com.github.azuazu3939.azPlugin.lib.conditions.ConditionsRecord;
import com.github.azuazu3939.azPlugin.lib.conditions.DataValue;
import com.github.azuazu3939.azPlugin.lib.conditions.ActionItemStack;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public record PacketBlockRegister(Location location, ConditionsRecord record, long tick, DataValue conditions) {

    private static final Map<Location, PacketBlockRegister> BLOCKS = new HashMap<>();

    public PacketBlockRegister(Location location, @NotNull ConditionsRecord record, long tick, DataValue conditions) {
        this.location = location;
        this.record = record;
        this.tick = tick;
        this.conditions = conditions;
        BLOCKS.put(location, this);
    }

    @Nullable
    public ItemStack getDrop() {
        PacketBlockRegister pbr = BLOCKS.get(location);
        if (pbr.record instanceof ActionItemStack item) {
            return (item.check()) ? item.item() : null;
        }
        return null;
    }

    public boolean setBlock() {
        PacketBlockRegister pbr = BLOCKS.get(location);
        if (pbr.record instanceof ActionBlock block) {
            return block.set();
        }
        return false;
    }

    @Nullable
    public static PacketBlockRegister checkAndGet(Location loc) {
        if (BLOCKS.containsKey(loc)) {
            return BLOCKS.get(loc);
        }
        return null;
    }

    public long tick() {
        return tick;
    }
}
