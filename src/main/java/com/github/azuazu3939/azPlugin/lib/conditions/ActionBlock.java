package com.github.azuazu3939.azPlugin.lib.conditions;

import com.github.azuazu3939.azPlugin.listener.PacketBlockListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public record ActionBlock(Location loc, Material material, long tick, DataValue conditions) implements ConditionsRecord {

    public boolean set(Player player) {
        if (conditions().check()) {
            return PacketBlockListener.process(player, loc, material, tick);
        }
        return false;
    }
}
