package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.PacketHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
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
        checkAndDisplay(player);
        return true;
    }

    public static void setPos1(@NotNull Player player, @NotNull Location loc) {
        POS1.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos1 " + loc.getX() + " " + loc.getY() + " " + loc.getZ()));
    }

    public static void setPos2(@NotNull Player player, @NotNull Location loc) {
        POS2.put(player.getUniqueId(), new Vector(loc.getX(), loc.getY(), loc.getZ()));
        player.sendMessage(Component.text("//pos2 " + loc.getX() + " " + loc.getY() + " " + loc.getZ()));
    }

    public static void checkAndDisplay(@NotNull Player player) {
        if (POS1.containsKey(player.getUniqueId()) && POS2.containsKey(player.getUniqueId())) {
            Vector pos1 = POS1.get(player.getUniqueId());
            Vector pos2 = POS2.get(player.getUniqueId());

            player.sendMessage(Component.text("金の斧を持っている間のみ枠が表示されます"));
            AREA.put(player.getUniqueId(), new BoundingBox(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(), pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()));
            outline(player, Material.LIME_STAINED_GLASS);
        }
    }

    @Nullable
    public static BoundingBox getArea(@NotNull Player player) {
        if (AREA.containsKey(player.getUniqueId())) {
            return AREA.get(player.getUniqueId());
        }
        return null;
    }

    private static void send(Player player, @NotNull Vector v, int id, Material m) {
        PacketHandler.spawnBlockDisplay(player, v.getBlockX() + 0.5, v.getBlockY(), v.getBlockZ() + 0.5, id);
        PacketHandler.setBlockDisplayMeta(player, id, m);
        AzPlugin.getInstance().runAsyncLater(()-> PacketHandler.removePacketEntity(player, id), 20);
    }

    private static void outline(@NotNull Player player, Material material) {
        if (!AREA.containsKey(player.getUniqueId())) return;
        if (player == null || player.getInventory().getItemInMainHand().getType() != Material.GOLDEN_AXE) return;
        BoundingBox box = AREA.get(player.getUniqueId());

        Vector pos1 = box.getMin();
        Vector pos2 = box.getMax();

        AzPlugin.getInstance().runAsync(()-> {

            Set<Vector> outline = new HashSet<>();

            for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
                outline.add(new Vector(x, pos1.getBlockY(), pos1.getBlockZ()));
                outline.add(new Vector(x, pos1.getBlockY(), pos2.getBlockZ()));
                outline.add(new Vector(x, pos2.getBlockY(), pos1.getBlockZ()));
                outline.add(new Vector(x, pos2.getBlockY(), pos2.getBlockZ()));
            }

            for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
                outline.add(new Vector(pos1.getBlockX(), pos1.getBlockY(), z));
                outline.add(new Vector(pos1.getBlockX(), pos2.getBlockY(), z));
                outline.add(new Vector(pos2.getBlockX(), pos1.getBlockY(), z));
                outline.add(new Vector(pos2.getBlockX(), pos2.getBlockY(), z));
            }

            for (int y = pos1.getBlockY(); y <= pos2.getBlockY(); y++) {
                outline.add(new Vector(pos1.getBlockX(), y, pos1.getBlockZ()));
                outline.add(new Vector(pos1.getBlockX(), y, pos2.getBlockZ()));
                outline.add(new Vector(pos2.getBlockX(), y, pos1.getBlockZ()));
                outline.add(new Vector(pos2.getBlockX(), y, pos2.getBlockZ()));
            }

            Random rand = new Random();
            for (Vector vector : outline) {
                send(player, vector, rand.nextInt(Integer.MAX_VALUE), material);
            }
        });
        AzPlugin.getInstance().runLater(()-> outline(player, material), 20);
    }
}
