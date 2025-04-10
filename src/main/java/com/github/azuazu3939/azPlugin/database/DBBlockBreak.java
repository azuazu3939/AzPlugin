package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockBreak extends DBCon {

    private static final Map<AbstractLocationSet, String> BREAK_ACTION = new ConcurrentHashMap<>();

    public static void updateLocationAsync(AbstractLocationSet set, String trigger) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(set, trigger));
    }

    public static void updateLocationSync(AbstractLocationSet set, String trigger) {
        try {
            runPrepareStatement("INSERT INTO `" + BREAK + "` " +
                    "(`name`, `x`, `y`, `z`, trigger`)" +
                    " VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "trigger` =?;", preparedStatement -> {

                preparedStatement.setString(1, set.world().getName());
                preparedStatement.setInt(2, set.x());
                preparedStatement.setInt(3, set.y());
                preparedStatement.setInt(4, set.z());
                preparedStatement.setString(5, trigger);
                preparedStatement.setString(6, trigger);
                preparedStatement.execute();
                setBreak(set);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BREAK_ACTION.put(set, trigger);
    }

    @NotNull
    public static Optional<String> getLocationAction(@NotNull AbstractLocationSet set) {
        if (BREAK_ACTION.containsKey(set)) {
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
                                BREAK_ACTION.put(set, rs.getString("trigger"));
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
}
