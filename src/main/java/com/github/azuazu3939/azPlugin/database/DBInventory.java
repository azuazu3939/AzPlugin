package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.packet.BlockInteractAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBInventory extends DBCon {

    private static final Map<String, BlockInteractAction> TEMP_INVENTORY = new ConcurrentHashMap<>();

    public static void updateBlockInteractAsync(String key, Inventory inv, ItemStack cursor) {
        AzPlugin.getInstance().runAsync(()-> updateBlockInteractSync(key, inv, cursor));
    }

    public static void updateBlockInteractSync(String key, Inventory inv, ItemStack cursor) {
        try {
            runPrepareStatement("INSERT INTO `" + INTERACT + "` " +
                    "(`shop`, `slot`, `item`, `cursor`)" +
                    " VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`item`=?, `cursor` =?;", preparedStatement -> {
                for (int i = 0; i < inv.getSize(); i++) {
                    preparedStatement.setString(1, key);
                    preparedStatement.setInt(2, i);

                    ItemStack item = inv.getItem(i);
                    preparedStatement.setBytes(3, (item == null) ? null : item.serializeAsBytes());
                    preparedStatement.setBytes(4, (cursor == null) ? null : cursor.serializeAsBytes());
                    preparedStatement.setBytes(5, (item == null) ? null : item.serializeAsBytes());
                    preparedStatement.setBytes(6, (cursor == null) ? null : cursor.serializeAsBytes());
                    preparedStatement.execute();
                }
                TEMP_INVENTORY.put(key, new BlockInteractAction(inv, cursor));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockInteractAction> getLocationAction(@NotNull String key, Inventory inv) {
        if (TEMP_INVENTORY.containsKey(key)) {
            return Optional.of(TEMP_INVENTORY.get(key));
        } else {
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT * FROM `" + INVENTORY + "` WHERE `shop` = ?;", preparedStatement -> {
                        preparedStatement.setString(1, key);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            while (rs.next()) {
                                byte[] itemBytes = rs.getBytes("item");
                                ItemStack item = (itemBytes == null) ? null : ItemStack.deserializeBytes(itemBytes);
                                inv.setItem(rs.getInt("slot"), item);
                            }
                            byte[] cursorBytes = rs.getBytes("cursor");
                            ItemStack cursorItem = (cursorBytes == null) ? null : ItemStack.deserializeBytes(cursorBytes);
                            TEMP_INVENTORY.put(key, new BlockInteractAction(inv, cursorItem));
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
}
