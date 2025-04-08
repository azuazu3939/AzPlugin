package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.packet.BlockBreakAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockBreak extends DBCon {

    private static final Map<Location, BlockBreakAction> BREAK_ACTION = new ConcurrentHashMap<>();

    public static void updateLocationAsync(Location loc, int tick, String mmid, int amount, double chance, Material ct_material) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(loc, tick, mmid, amount, chance, ct_material));
    }

    public static void updateLocationSync(Location loc, int tick, String mmid, int amount, double chance, Material ct_material) {
        try {
            runPrepareStatement("INSERT INTO `" + BREAK + "` " +
                    "(`name`, `x`, `y`, `z`, `tick`, `mmid`, `amount`, `chance`, `ct_material`)" +
                    " VALUES (?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`tick` = ?, `mmid` = ?, `amount` = ?, `chance` =?, `ct_material` =?;", preparedStatement -> {

                preparedStatement.setString(1, loc.getWorld().getName());
                preparedStatement.setInt(2, loc.getBlockX());
                preparedStatement.setInt(3, loc.getBlockY());
                preparedStatement.setInt(4, loc.getBlockZ());
                preparedStatement.setInt(5, tick);
                preparedStatement.setString(6, mmid);
                preparedStatement.setInt(7, amount);
                preparedStatement.setDouble(8, chance);
                preparedStatement.setString(9, ct_material == null ? null : ct_material.toString());

                preparedStatement.setInt(10, tick);
                preparedStatement.setString(11, mmid);
                preparedStatement.setInt(12, amount);
                preparedStatement.setDouble(13, chance);
                preparedStatement.setString(14, ct_material == null ? null : ct_material.toString());
                preparedStatement.execute();
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BREAK_ACTION.put(loc, new BlockBreakAction(tick, mmid, amount, chance, ct_material));
    }

    @NotNull
    public static Optional<BlockBreakAction> getLocationAction(@NotNull Block block) {
        Location loc = block.getLocation();
        if (BREAK_ACTION.containsKey(loc)) {
            return Optional.of(BREAK_ACTION.get(loc));
        } else {
            String name = block.getWorld().getName();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + BREAK + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?", preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, x);
                        preparedStatement.setInt(3, y);
                        preparedStatement.setInt(4, z);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                Material cm = rs.getString("ct_material") == null ? null : Material.valueOf(rs.getString("ct_material").toUpperCase());
                                BREAK_ACTION.put(loc, new BlockBreakAction(
                                        rs.getInt("tick"),
                                        rs.getString("mmid"),
                                        rs.getInt("amount"),
                                        rs.getDouble("chance"),
                                        cm));
                            }
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (BREAK_ACTION.containsKey(loc)) ? Optional.of(BREAK_ACTION.get(loc)) : Optional.empty();
        }
    }
}
