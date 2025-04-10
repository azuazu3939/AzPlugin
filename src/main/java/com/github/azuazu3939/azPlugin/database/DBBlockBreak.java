package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockBreakAction;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockBreak extends DBCon {

    private static final Map<AbstractLocationSet, BlockBreakAction> BREAK_ACTION = new ConcurrentHashMap<>();

    public static void updateLocationAsync(AbstractLocationSet set, String trigger, int tick, Material material) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(set, trigger, tick, material));
    }

    public static void updateLocationSync(AbstractLocationSet set, String trigger, int tick, Material material) {
        try {
            runPrepareStatement("INSERT INTO `" + BREAK + "` " +
                    "(`name`, `x`, `y`, `z`, `trigger`, `tick`, `material`)" +
                    " VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`trigger` =?, `tick` =?, `material` =?;", preparedStatement -> {

                preparedStatement.setString(1, set.world().getName());
                preparedStatement.setInt(2, set.x());
                preparedStatement.setInt(3, set.y());
                preparedStatement.setInt(4, set.z());
                preparedStatement.setString(5, trigger);
                preparedStatement.setInt(6, tick);
                preparedStatement.setString(7,(material == null ? null : material.name()));

                preparedStatement.setString(8, trigger);
                preparedStatement.setInt(9, tick);
                preparedStatement.setString(10,(material == null ? null : material.name()));
                preparedStatement.execute();
                setBreak(set);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BREAK_ACTION.put(set, new BlockBreakAction(trigger, tick, material));
    }

    @NotNull
    public static Optional<BlockBreakAction> getLocationAction(@NotNull AbstractLocationSet set) {
        if (!BREAK_ACTION.isEmpty() && BREAK_ACTION.containsKey(set)) {
            return Optional.of(BREAK_ACTION.get(set));
        } else {
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + BREAK + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?", preparedStatement -> {
                        preparedStatement.setString(1, set.world().getName());
                        preparedStatement.setInt(2, set.x());
                        preparedStatement.setInt(3, set.y());
                        preparedStatement.setInt(4, set.z());
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                Material material = rs.getString("material") == null ? null : Material.getMaterial(rs.getString("material"));
                                BREAK_ACTION.put(set, new BlockBreakAction(
                                        rs.getString("trigger"),
                                        rs.getInt("tick"),
                                        material));
                            }
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (BREAK_ACTION.containsKey(set)) ? Optional.of(BREAK_ACTION.get(set)) : Optional.empty();
        }
    }

    public static void clear() {
        BREAK_ACTION.clear();
    }

    public static void delete(String key) {
        AzPlugin.getInstance().runAsync(()-> {
            try {
                runPrepareStatement("DELETE FROM `" + BREAK + "` WHERE `trigger` =?;", preparedStatement -> {
                    preparedStatement.setString(1, key);
                    preparedStatement.execute();
                    clear();
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
