package com.github.azuazu3939.azPlugin.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SetCommandUtil {

    @NotNull
    public static Set<Location> getLocations(Player player, BoundingBox box, Material material) {
        Set<Location> locations = new HashSet<>();
        if (player == null) return locations;
        World world = player.getWorld();

        Vector min = box.getMin();
        Vector max = box.getMax();
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {

                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() != material) continue;
                    locations.add(b.getLocation());
                }
            }
        }
        return locations;
    }
}
