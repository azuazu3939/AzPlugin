package com.github.azuazu3939.azPlugin.lib.conditions;

import org.bukkit.Location;
import org.bukkit.Material;

public record ActionBlock(Location loc, Material material, long tick, DataValue conditions) implements ConditionsRecord {

    public boolean set() {
        return conditions().check();
    }
}
