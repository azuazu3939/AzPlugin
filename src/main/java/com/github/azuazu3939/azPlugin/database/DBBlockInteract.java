package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.packet.BlockInteractAction;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockInteract extends DBCon {

    private static final Map<Location, BlockInteractAction> INTERACT_ACTION = new ConcurrentHashMap<>();

    public static void updateBlockInteractAsync(Location loc, String key, Inventory inv, ItemStack cursor) {
        AzPlugin.getInstance().runAsync(() -> updateBlockInteractSync(loc, key, inv, cursor));
    }

    public static void updateBlockInteractSync(Location loc, String key, Inventory inv, ItemStack cursor) {
        try {
            runPrepareStatement("INSERT INTO `" + BREAK + "` " +
                    "(`name`, `x`, `y`, `z`, `shop_key`, `slot`, `item`, `cursor`)" +
                    " VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`item`=?, `cursor` =?;", preparedStatement -> {
                for (int i = 0; i < inv.getSize(); i++) {
                    preparedStatement.setString(1, loc.getWorld().getName());
                    preparedStatement.setInt(2, loc.getBlockX());
                    preparedStatement.setInt(3, loc.getBlockY());
                    preparedStatement.setInt(4, loc.getBlockZ());
                    preparedStatement.setString(5, key);
                    preparedStatement.setInt(6, i);

                    ItemStack item = inv.getItem(i);
                    preparedStatement.setBytes(7, (item == null) ? null : item.serializeAsBytes());
                    preparedStatement.setBytes(8, (cursor == null) ? null : cursor.serializeAsBytes());
                    preparedStatement.execute();
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockInteractAction> getLocationAction(@NotNull Block block, Inventory inv) {
        Location loc = block.getLocation();
        if (INTERACT_ACTION.containsKey(loc)) {
            return Optional.of(INTERACT_ACTION.get(loc));
        } else {
            String name = block.getWorld().getName();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT * FROM `" + INTERACT + "` WHERE `name` = ? AND `x` = ? AND `y` = ? AND `z` = ?", preparedStatement -> {
                        preparedStatement.setString(1, name);
                        preparedStatement.setInt(2, x);
                        preparedStatement.setInt(3, y);
                        preparedStatement.setInt(4, z);
                        try (ResultSet rs = preparedStatement.executeQuery()) {
                            while (rs.next()) {
                                byte[] itemBytes = rs.getBytes("item");
                                ItemStack item = (itemBytes == null) ? null : ItemStack.deserializeBytes(itemBytes);
                                inv.setItem(rs.getInt("slot"), item);
                            }
                            byte[] cursorBytes = rs.getBytes("cursor");
                            ItemStack cursorItem = (cursorBytes == null) ? null : ItemStack.deserializeBytes(cursorBytes);;
                            INTERACT_ACTION.put(loc, new BlockInteractAction(rs.getString("shop_key"), inv, cursorItem));
                        }

                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            return (INTERACT_ACTION.containsKey(loc)) ? Optional.of(INTERACT_ACTION.get(loc)) : Optional.empty();
        }
    }
}
