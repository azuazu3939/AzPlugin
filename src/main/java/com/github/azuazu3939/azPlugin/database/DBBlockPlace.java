package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockPlaceAction;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockPlace extends DBCon {

    private static final Map<AbstractLocationSet, BlockPlaceAction> PLACE_ACTION = new ConcurrentHashMap<>();

    public static void updateLocationAsync(AbstractLocationSet set, int tick, Material material, String trigger) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(set, tick, material, trigger));
    }

    public static void updateLocationSync(AbstractLocationSet set, int tick, Material material, String trigger) {
        try {
            runPrepareStatement("INSERT INTO `" + PLACE +"` " +
                    "(`name`, `x`, `y`, `z`, `tick`, `material`, `trigger`)" +
                    " VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`tick` = ?, `material` =?, `trigger` =?;", preparedStatement -> {

                preparedStatement.setString(1, set.world().getName());
                preparedStatement.setInt(2, set.x());
                preparedStatement.setInt(3, set.y());
                preparedStatement.setInt(4, set.z());
                preparedStatement.setInt(5, tick);
                preparedStatement.setString(6, material == null ? null : material.toString());
                preparedStatement.setString(7, trigger);

                preparedStatement.setInt(8, tick);
                preparedStatement.setString(19, material == null ? null : material.toString());
                preparedStatement.setString(10, trigger);
                preparedStatement.execute();
                setPlace(set);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        PLACE_ACTION.put(set, new BlockPlaceAction(tick, material, trigger));
    }

    @NotNull
    public static Optional<BlockPlaceAction> getLocationAction(@NotNull AbstractLocationSet set) {
        if (PLACE_ACTION.containsKey(set)) {
            return Optional.of(PLACE_ACTION.get(set));
        } else {
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + PLACE + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?", preparedStatement -> {
                        preparedStatement.setString(1, set.world().getName());
                        preparedStatement.setInt(2, set.x());
                        preparedStatement.setInt(3, set.y());
                        preparedStatement.setInt(4, set.z());
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                Material material = rs.getString("material") == null ? null : Material.valueOf(rs.getString("material").toUpperCase());
                                PLACE_ACTION.put(set, new BlockPlaceAction(
                                        rs.getInt("tick"),
                                        material,
                                        rs.getString("trigger")
                                ));
                            }
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (PLACE_ACTION.containsKey(set)) ? Optional.of(PLACE_ACTION.get(set)) : Optional.empty();
        }
    }


    public static void clear() {
        PLACE_ACTION.clear();
    }
}
