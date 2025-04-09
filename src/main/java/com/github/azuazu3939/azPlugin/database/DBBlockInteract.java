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

public class DBBlockInteract extends DBCon {

    private static final Map<AbstractLocationSet, BlockInteractAction> INTERACT_ACTION = new ConcurrentHashMap<>();

    public static void updateBlockInteractAsync(AbstractLocationSet set, Inventory inv, ItemStack cursor) {
        AzPlugin.getInstance().runAsync(() -> updateBlockInteractSync(set, inv, cursor));
    }

    public static void updateBlockInteractSync(AbstractLocationSet set, Inventory inv, ItemStack cursor) {
        try {
            runPrepareStatement("INSERT INTO `" + BREAK + "` " +
                    "(`name`, `x`, `y`, `z`, `slot`, `item`, `cursor`)" +
                    " VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`item`=?, `cursor` =?;", preparedStatement -> {
                for (int i = 0; i < inv.getSize(); i++) {
                    preparedStatement.setString(1, set.world().getName());
                    preparedStatement.setInt(2, set.x());
                    preparedStatement.setInt(3, set.y());
                    preparedStatement.setInt(4, set.z());
                    preparedStatement.setInt(5, i);

                    ItemStack item = inv.getItem(i);
                    preparedStatement.setBytes(6, (item == null) ? null : item.serializeAsBytes());
                    preparedStatement.setBytes(7, (cursor == null) ? null : cursor.serializeAsBytes());
                    preparedStatement.execute();
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockInteractAction> getLocationAction(@NotNull AbstractLocationSet set, Inventory inv) {
        if (INTERACT_ACTION.containsKey(set)) {
            return Optional.of(INTERACT_ACTION.get(set));
        } else {
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT * FROM `" + INTERACT + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?;", preparedStatement -> {
                        preparedStatement.setString(1, set.world().getName());
                        preparedStatement.setInt(2, set.x());
                        preparedStatement.setInt(3, set.y());
                        preparedStatement.setInt(4, set.z());
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            while (rs.next()) {
                                byte[] itemBytes = rs.getBytes("item");
                                ItemStack item = (itemBytes == null) ? null : ItemStack.deserializeBytes(itemBytes);
                                inv.setItem(rs.getInt("slot"), item);
                            }
                            byte[] cursorBytes = rs.getBytes("cursor");
                            ItemStack cursorItem = (cursorBytes == null) ? null : ItemStack.deserializeBytes(cursorBytes);;
                            INTERACT_ACTION.put(set, new BlockInteractAction(inv, cursorItem));
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (INTERACT_ACTION.containsKey(set)) ? Optional.of(INTERACT_ACTION.get(set)) : Optional.empty();
        }
    }
}
