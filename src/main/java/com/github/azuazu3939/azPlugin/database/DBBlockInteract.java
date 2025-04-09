package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockInteract extends DBCon {

    private static final Map<AbstractLocationSet, String> INTERACT_ACTION = new ConcurrentHashMap<>();

    public static void updateBlockInteractAsync(AbstractLocationSet set, String key) {
        AzPlugin.getInstance().runAsync(() -> updateBlockInteractSync(set, key));
    }

    public static void updateBlockInteractSync(AbstractLocationSet set, String key) {
        try {
            runPrepareStatement("INSERT INTO `" + INTERACT + "` " +
                    "(`name`, `x`, `y`, `z`, `shop`)" +
                    " VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`shop`=?;", preparedStatement -> {
                preparedStatement.setString(1, set.world().getName());
                preparedStatement.setInt(2, set.x());
                preparedStatement.setInt(3, set.y());
                preparedStatement.setInt(4, set.z());
                preparedStatement.setString(5, key);
                preparedStatement.setString(6, key);
                preparedStatement.execute();
                INTERACT_ACTION.put(set, key);
                setInteract(set);

            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<String> getLocationAction(@NotNull AbstractLocationSet set) {
        if (INTERACT_ACTION.containsKey(set)) {
            return Optional.of(INTERACT_ACTION.get(set));
        } else {
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT `shop` FROM `" + INTERACT + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?;", preparedStatement -> {
                        preparedStatement.setString(1, set.world().getName());
                        preparedStatement.setInt(2, set.x());
                        preparedStatement.setInt(3, set.y());
                        preparedStatement.setInt(4, set.z());
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            if (rs.next()) {
                                INTERACT_ACTION.put(set, rs.getString("shop"));
                            }
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (INTERACT_ACTION.containsKey(set)) ? Optional.of(INTERACT_ACTION.get(set)) : Optional.empty();
        }
    }

    public static void clear() {
        INTERACT_ACTION.clear();
    }
}
