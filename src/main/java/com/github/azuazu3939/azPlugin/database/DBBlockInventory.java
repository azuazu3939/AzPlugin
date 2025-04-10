package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockInteractAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockInventory extends DBCon {

    private static final Map<String, BlockInteractAction> TEMP_INVENTORY = new ConcurrentHashMap<>();

    public static void updateBlockInteractAsync(String key, Inventory inv, ItemStack cursor) {
        AzPlugin.getInstance().runAsync(()-> updateBlockInteractSync(key, inv, cursor));
    }

    public static void updateBlockInteractSync(String key, Inventory inv, ItemStack cursor) {
        try {
            runPrepareStatement("INSERT INTO `" + INVENTORY + "` " +
                    "(`shop`, `item`, `cursor`)" +
                    " VALUES (?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`item`=?, `cursor` =?;", preparedStatement -> {

                preparedStatement.setString(1, key);

                byte[] bytes = ItemStack.serializeItemsAsBytes(inv.getContents());
                preparedStatement.setBytes(2, bytes);
                preparedStatement.setBytes(3, (cursor == null || cursor.getType().isAir()) ? null : cursor.serializeAsBytes());
                preparedStatement.setBytes(4, bytes);
                preparedStatement.setBytes(5, (cursor == null || cursor.getType().isAir()) ? null : cursor.serializeAsBytes());
                preparedStatement.execute();
                TEMP_INVENTORY.put(key, new BlockInteractAction(inv, cursor));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockInteractAction> getLocationAction(@NotNull String key, Inventory inv) {
        if (!TEMP_INVENTORY.isEmpty() && TEMP_INVENTORY.containsKey(key)) {
            return Optional.of(TEMP_INVENTORY.get(key));
        } else {
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT * FROM `" + INVENTORY + "` WHERE `shop` = ?;", preparedStatement -> {
                        preparedStatement.setString(1, key);
                        try (ResultSet rs = preparedStatement.executeQuery()) {

                            if (rs.next()) {
                                byte[] itemBytes = rs.getBytes("item");
                                ItemStack[] items = ItemStack.deserializeItemsFromBytes(itemBytes);

                                int i = 0;
                                for (ItemStack item : items) {
                                    inv.setItem(i, item);
                                    i++;
                                }

                                byte[] cursorBytes = rs.getBytes("cursor");
                                ItemStack cursor = (cursorBytes == null) ? null : ItemStack.deserializeBytes(cursorBytes);
                                TEMP_INVENTORY.put(key, new BlockInteractAction(inv, cursor));
                            }
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (TEMP_INVENTORY.containsKey(key)) ? Optional.of(TEMP_INVENTORY.get(key)) : Optional.empty();
        }
    }

    public static void clear() {
        TEMP_INVENTORY.clear();
    }

    @NotNull
    @Contract(pure = true)
    public static Collection<String> get() {return TEMP_INVENTORY.keySet();
    }
}
