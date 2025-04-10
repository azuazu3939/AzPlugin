package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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

    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;

        if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return false;
        Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 1);

        if (s.endsWith("1")) return setPos1(player, player.getLocation());
        if (s.endsWith("2")) return setPos2(player, player.getLocation());
        return false;
    }

    public static boolean setPos1(@NotNull Player player, @NotNull Location loc) {
        POS1.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos1 " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
        return checkAndDisplay(player);
    }

    public static boolean setPos2(@NotNull Player player, @NotNull Location loc) {
        POS2.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos2 " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
        return checkAndDisplay(player);
    }

    public static boolean checkAndDisplay(@NotNull Player player) {
        if (POS1.containsKey(player.getUniqueId()) && POS2.containsKey(player.getUniqueId())) {
            Vector pos1 = POS1.get(player.getUniqueId());
            Vector pos2 = POS2.get(player.getUniqueId());

            AREA.put(player.getUniqueId(), new BoundingBox(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
        }
        return true;
    }

    @Nullable
    public static BoundingBox getArea(@NotNull Player player) {
        if (AREA.containsKey(player.getUniqueId())) {
            return AREA.get(player.getUniqueId());
        }
        return null;
    }
}
