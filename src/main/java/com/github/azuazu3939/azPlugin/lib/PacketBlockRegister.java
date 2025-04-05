package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.lib.conditions.ActionBlock;
import com.github.azuazu3939.azPlugin.lib.conditions.ConditionsRecord;
import com.github.azuazu3939.azPlugin.lib.conditions.DataValue;
import com.github.azuazu3939.azPlugin.lib.conditions.ActionItemStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public record PacketBlockRegister(Location location, ConditionsRecord record, DataValue conditions) {

    private static final Map<Location, PacketBlockRegister> BLOCKS = new HashMap<>();

    public PacketBlockRegister(Location location, @NotNull ConditionsRecord record, DataValue conditions) {
        this.location = location;
        this.record = record;
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

    public boolean setBlock(Player player) {
        PacketBlockRegister pbr = BLOCKS.get(location);
        if (pbr.record instanceof ActionBlock block) {
            return block.set(player);
        }
        return false;
    }
}
