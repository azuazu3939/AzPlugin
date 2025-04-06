package com.github.azuazu3939.azPlugin.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PositionCommand implements CommandExecutor {


    private static final Map<UUID, BoundingBox> AREA = new ConcurrentHashMap<>();
    private static final Map<UUID, Vector> POS1 = new HashMap<>();
    private static final Map<UUID, Vector> POS2 = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (s.endsWith("1")) {
            setPos1(player, player.getLocation());

        } else if (s.endsWith("2")) {
            setPos2(player, player.getLocation());
        }
        return true;
    }

    public static void setPos1(@NotNull Player player, @NotNull Location loc) {
        POS1.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos1 " + loc.getX() + " " + loc.getY() + " " + loc.getZ()));
        checkAndDisplay(player);
    }

    public static void setPos2(@NotNull Player player, @NotNull Location loc) {
        POS2.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos2 " + loc.getX() + " " + loc.getY() + " " + loc.getZ()));
        checkAndDisplay(player);
    }

    public static void checkAndDisplay(@NotNull Player player) {
        if (POS1.containsKey(player.getUniqueId()) && POS2.containsKey(player.getUniqueId())) {
            Vector pos1 = POS1.get(player.getUniqueId());
            Vector pos2 = POS2.get(player.getUniqueId());

            AREA.put(player.getUniqueId(), new BoundingBox(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
        }
    }

    @Nullable
    public static BoundingBox getArea(@NotNull Player player) {
        if (AREA.containsKey(player.getUniqueId())) {
            return AREA.get(player.getUniqueId());
        }
        return null;
    }
}
