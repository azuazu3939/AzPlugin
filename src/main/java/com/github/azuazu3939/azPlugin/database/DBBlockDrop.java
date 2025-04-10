package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockDropAction;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockDrop extends DBCon {

    private static final Map<String, BlockDropAction> TEMP_DROP = new ConcurrentHashMap<>();

    public static void updateLocationAsync(int tick, Material material, String trigger, String mmid, int amount, double chance) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(tick, material, trigger, mmid, amount, chance));
    }

    public static void updateLocationSync(int tick, Material material, String trigger, String mmid, int amount, double chance) {
        try {
            runPrepareStatement("INSERT INTO `" + DROP + "` " +
                    "(`trigger`, `material`, `tick`, `mmid`, `amount`, `chance`)" +
                    " VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`material` =?, `tick` =?, `mmid` =?, `amount` =?, `chance` =?;", preparedStatement -> {
                preparedStatement.setString(1, trigger);
                preparedStatement.setString(2, (material == null) ? null : material.toString());
                preparedStatement.setInt(3, tick);
                preparedStatement.setString(4, mmid);
                preparedStatement.setInt(5, amount);
                preparedStatement.setDouble(6, chance);

                preparedStatement.setString(7, (material == null) ? null : material.toString());
                preparedStatement.setInt(8, tick);
                preparedStatement.setDouble(9, amount);
                preparedStatement.setDouble(10, chance);
                preparedStatement.execute();
                TEMP_DROP.put(trigger, new BlockDropAction(mmid, amount, chance, tick, material));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockDropAction> getBlockDropAction(String trigger) {
        if (TEMP_DROP.containsKey(trigger)) {
            return Optional.of(TEMP_DROP.get(trigger));
        } else {
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + DROP + "` WHERE `trigger` =?", preparedStatement -> {
                        preparedStatement.setString(1, trigger);
                        try (ResultSet rs = preparedStatement.executeQuery()) {

                            if (rs.next()) {
                                Material material = Material.getMaterial(rs.getString("material"));
                                TEMP_DROP.put(trigger, new BlockDropAction(
                                        rs.getString("mmid"),
                                        rs.getInt("amount"),
                                        rs.getDouble("chance"),
                                        rs.getInt("tick"),
                                        material));
                            }
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return TEMP_DROP.containsKey(trigger) ? Optional.of(TEMP_DROP.get(trigger)) : Optional.empty();
    }

    public static void clear() {
        TEMP_DROP.clear();
    }
}
