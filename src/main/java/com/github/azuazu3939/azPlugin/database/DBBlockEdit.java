package com.github.azuazu3939.azPlugin.database;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockEditAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DBBlockEdit extends DBCon {

    private static final Map<String, BlockEditAction> TEMP_EDIT = new ConcurrentHashMap<>();

    public static void updateLocationAsync(Set<AbstractLocationSet> set, int tick, Material material, String trigger) {
        AzPlugin.getInstance().runAsync(() -> updateLocationSync(set, tick, material, trigger));
    }

    public static void updateLocationSync(Set<AbstractLocationSet> sets, int tick, Material material, String trigger) {
        try {
            runPrepareStatement("INSERT INTO `" + EDIT + "` " +
                    "(`name`, `x`, `y`, `z`, `tick`, `material`, `trigger`)" +
                    " VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "`tick` = ?, `material` =?;", preparedStatement -> {

                for (AbstractLocationSet set : sets) {
                    preparedStatement.setString(1, set.world().getName());
                    preparedStatement.setInt(2, set.x());
                    preparedStatement.setInt(3, set.y());
                    preparedStatement.setInt(4, set.z());
                    preparedStatement.setInt(5, tick);
                    preparedStatement.setString(6, material == null ? null : material.toString());
                    preparedStatement.setString(7, trigger);

                    preparedStatement.setInt(8, tick);
                    preparedStatement.setString(9, material == null ? null : material.toString());
                    preparedStatement.execute();
                }
                TEMP_EDIT.put(trigger, new BlockEditAction(sets, tick, material));
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static Optional<BlockEditAction> getBlockEditAction(String trigger) {
        if (!TEMP_EDIT.isEmpty()  && TEMP_EDIT.containsKey(trigger)) {
            return Optional.of(TEMP_EDIT.get(trigger));
        } else {
            AzPlugin.getInstance().runAsync(() -> {
                try {
                    runPrepareStatement("SELECT `tick`, `material` FROM `" + EDIT + "` WHERE `trigger` =?", preparedStatement -> {
                        preparedStatement.setString(1, trigger);
                        try (ResultSet rs = preparedStatement.executeQuery()) {

                            Set<AbstractLocationSet> set = ConcurrentHashMap.newKeySet();
                            int tick = 200;
                            Material m = Material.AIR;

                            boolean triggered = false;
                            while (rs.next()) {
                                triggered = true;
                                tick = rs.getInt("tick");
                                m = (rs.getString("material") == null) ? Material.AIR : Material.valueOf(rs.getString("material").toUpperCase());
                                World w = Bukkit.getWorld(rs.getString("name"));
                                if (w == null) continue;
                                set.add(new AbstractLocationSet(w, rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
                            }
                            if (triggered) {
                                TEMP_EDIT.put(trigger, new BlockEditAction(set, tick, m));
                            }
                        }
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return TEMP_EDIT.containsKey(trigger) ? Optional.of(TEMP_EDIT.get(trigger)) : Optional.empty();
    }

    public static void clear() {TEMP_EDIT.clear();}

    @NotNull
    @Contract(pure = true)
    public static Collection<String> get() {return TEMP_EDIT.keySet();}
}