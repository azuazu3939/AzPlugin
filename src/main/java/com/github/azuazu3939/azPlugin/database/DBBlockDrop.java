package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockDropAction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockDrop extends DBCon {

    private static final Map<String, BlockDropAction> TEMP_DROP = new ConcurrentHashMap<>();

    //TO-DO 複数ドロップ
    public static void updateLocationAsync(String trigger, String mmid, int amount, double chance) {
        AzPlugin.getInstance().runAsync(()-> updateLocationSync(trigger, mmid, amount, chance));
    }

    public static void updateLocationSync(String trigger, String mmid, int amount, double chance) {
        try {
            runPrepareStatement("INSERT INTO `" + DROP + "` " +
                    "(`trigger`, `mmid`, `amount`, `chance`)" +
                    " VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`mmid` =?, `amount` =?, `chance` =?;", preparedStatement -> {
                preparedStatement.setString(1, trigger);
                preparedStatement.setString(2, mmid);
                preparedStatement.setInt(3, amount);
                preparedStatement.setDouble(4, chance);

                preparedStatement.setString(5, mmid);
                preparedStatement.setInt(6, amount);
                preparedStatement.setDouble(7, chance);
                preparedStatement.execute();
                TEMP_DROP.put(trigger, new BlockDropAction(mmid, amount, chance));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockDropAction> getBlockDropAction(String trigger) {
        if (!TEMP_DROP.isEmpty() && TEMP_DROP.containsKey(trigger)) {
            return Optional.of(TEMP_DROP.get(trigger));
        } else {
            AzPlugin.getInstance().runAsync(()-> {
                try {
                    runPrepareStatement("SELECT * FROM `" + DROP + "` WHERE `trigger` =?", preparedStatement -> {
                        preparedStatement.setString(1, trigger);
                        try (ResultSet rs = preparedStatement.executeQuery()) {

                            if (rs.next()) {
                                TEMP_DROP.put(trigger, new BlockDropAction(
                                        rs.getString("mmid"),
                                        rs.getInt("amount"),
                                        rs.getDouble("chance")));
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

    @NotNull
    @Contract(pure = true)
    public static Collection<String> get() {return TEMP_DROP.keySet();}
}
