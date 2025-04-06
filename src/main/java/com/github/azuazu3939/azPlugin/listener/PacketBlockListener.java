package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.DBLocation;
import com.github.azuazu3939.azPlugin.lib.LocationAction;
import com.github.azuazu3939.azPlugin.lib.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PacketBlockListener implements Listener {

    private static final Multimap<UUID, BlockPos> packetBlocks = ArrayListMultimap.create();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getWorld().getName().toLowerCase().contains("open")) return;
        process(event.getPlayer(), block.getLocation(), Material.BEDROCK);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!block.getWorld().getName().toLowerCase().contains("open")) return;
        process(event.getPlayer(), block.getLocation(), event.getBlockPlaced().getType());
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        clear(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        clear(event.getPlayer());
    }

    private void process(@NotNull Player player, @NotNull Location loc, Material material) {
        Optional<LocationAction> op = DBLocation.getLocationAction(loc);
        if (op.isPresent()) {
            LocationAction action = op.get();
            long tick;
            String mmid = action.mmid();
            if (mmid != null) {
                tick = action.tick();
                ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, action.amount());
                Utils.dropItem(player, item);

            } else if (action.material() != null) {
                tick = action.tick();

            } else {
                tick = DBLocation.DEFAULT_TICK;
            }

            BlockPos ps = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (isAffected(player.getUniqueId(), ps)) {
                AzPlugin.getInstance().runAsync(() ->
                        PacketHandler.changeBlock(player, ps, material));
                return;
            }

            AzPlugin.getInstance().runAsyncLater(() -> {
                PacketHandler.changeBlock(player, ps, material);
                if (tick > 0) {
                    put(player.getUniqueId(), ps, tick);
                }
            }, 1);
        }
    }

    public static boolean isAffected(UUID uuid, BlockPos pos) {
        if (pos == null) return false;
        if (packetBlocks.containsKey(uuid)) {
            for (BlockPos p : packetBlocks.get(uuid)) {
                if (p.equals(pos)) return true;
            }
        }
        return false;
    }

    private static void put(UUID uuid, BlockPos pos, long tick) {
        packetBlocks.put(uuid, pos);
        if (tick > 0) {
            AzPlugin.getInstance().runAsyncLater(()-> {
                packetBlocks.remove(uuid, pos);
                PacketHandler.undoEffected(Bukkit.getPlayer(uuid), pos);
            }, tick);
        }
    }

    private void clear(@NotNull Player player) {
        packetBlocks.removeAll(player.getUniqueId());
    }
}
