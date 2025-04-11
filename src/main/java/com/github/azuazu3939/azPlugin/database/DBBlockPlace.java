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

    public static void updateLocationAsync(AbstractLocationSet set, String trigger, int tick, Material material, String mmid) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(set, trigger, tick, material, mmid));
    }

    public static void updateLocationSync(AbstractLocationSet set, String trigger, int tick, Material material, String mmid) {
        try {
            runPrepareStatement("INSERT INTO `" + PLACE +"` " +
                    "(`name`, `x`, `y`, `z`, `trigger`, `tick`, `material`, `mmid`)" +
                    " VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`trigger` =?, `tick` =?, `material` =?, `mmid` =?;", preparedStatement -> {

                preparedStatement.setString(1, set.world().getName());
                preparedStatement.setInt(2, set.x());
                preparedStatement.setInt(3, set.y());
                preparedStatement.setInt(4, set.z());
                preparedStatement.setString(5, trigger);
                preparedStatement.setInt(6, tick);
                preparedStatement.setString(7,(material == null ? null : material.name()));
                preparedStatement.setString(8, mmid);

                preparedStatement.setString(9, trigger);
                preparedStatement.setInt(10, tick);
                preparedStatement.setString(11,(material == null ? null : material.name()));
                preparedStatement.setString(12, mmid);
                preparedStatement.execute();
                setPlace(set);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        PLACE_ACTION.put(set, new BlockPlaceAction(tick, material, trigger, mmid));
    }

    @NotNull
    public static Optional<BlockPlaceAction> getLocationAction(@NotNull AbstractLocationSet set) {
        if (!PLACE_ACTION.isEmpty() && PLACE_ACTION.containsKey(set)) {
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
                                Material material = rs.getString("material") == null ? null : Material.getMaterial(rs.getString("material"));
                                PLACE_ACTION.put(set, new BlockPlaceAction(
                                        rs.getInt("tick"),
                                        material,
                                        rs.getString("trigger"),
                                        rs.getString("mmid")));
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

    public static void delete(String key) {
        AzPlugin.getInstance().runAsync(()-> {
            try {
                runPrepareStatement("DELETE FROM `" + PLACE + "` WHERE `trigger` =?;", preparedStatement -> {
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
