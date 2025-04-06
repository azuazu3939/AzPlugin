package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.LocationAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBLocation extends DBCon {

    private static final Map<Location, LocationAction> LOCATION_ACTION = new ConcurrentHashMap<>();

    public static final int DEFAULT_TICK = 200;

    public static void updateLocationAsync(Location loc, int tick, String mmid, int amount, Material material, double chance) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(loc, tick, mmid, amount, material, chance));
    }

    public static void updateLocationSync(Location loc, int tick, String mmid, int amount, Material material, double chance) {
        try {
            runPrepareStatement("INSERT INTO `" + LOCATION + "` " +
                    "(`name`, `x`, `y`, `z`, `tick`, `mmid`, `amount`, `material`, `chance`)" +
                    " VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`tick` = ?, `mmid` = ?, `amount` = ?, `material` = ?, `chance` =?;", preparedStatement -> {

                preparedStatement.setString(1, loc.getWorld().getName());
                preparedStatement.setInt(2, loc.getBlockX());
                preparedStatement.setInt(3, loc.getBlockY());
                preparedStatement.setInt(4, loc.getBlockZ());
                preparedStatement.setInt(5, tick);
                preparedStatement.setString(6, mmid);
                preparedStatement.setInt(7, amount);
                preparedStatement.setString(8, material == null ? null : material.toString());
                preparedStatement.setDouble(9, chance);
                preparedStatement.setInt(10, tick);
                preparedStatement.setString(11, mmid);
                preparedStatement.setInt(12, amount);
                preparedStatement.setString(13, material == null ? null : material.toString());
                preparedStatement.setDouble(14, chance);
                preparedStatement.execute();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        LOCATION_ACTION.put(loc, new LocationAction(tick, mmid, amount, material, chance));
    }

    @NotNull
    public static Optional<LocationAction> getLocationAction(Location loc) {
        if (LOCATION_ACTION.containsKey(loc)) {
            return Optional.of(LOCATION_ACTION.get(loc));
        } else {
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + LOCATION + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?", preparedStatement -> {
                        preparedStatement.setString(1, loc.getWorld().getName());
                        preparedStatement.setInt(2, loc.getBlockX());
                        preparedStatement.setInt(3, loc.getBlockY());
                        preparedStatement.setInt(4, loc.getBlockZ());
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                Material m = rs.getString("material") == null ? null : Material.valueOf(rs.getString("material").toUpperCase());
                                LOCATION_ACTION.put(loc, new LocationAction(
                                        rs.getInt("tick"),
                                        rs.getString("mmid"),
                                        rs.getInt("amount"),
                                        m, rs.getDouble("chance")));
                            }
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (LOCATION_ACTION.containsKey(loc)) ? Optional.of(LOCATION_ACTION.get(loc)) : Optional.empty();
        }
    }
}
