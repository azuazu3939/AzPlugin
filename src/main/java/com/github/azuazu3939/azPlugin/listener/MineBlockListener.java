package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.PacketHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MineBlockListener implements Listener {

    private static final Multimap<UUID, BlockPos> mineBlocks = ArrayListMultimap.create();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getWorld().getName().toLowerCase().contains("open")) return;
        Player player = event.getPlayer();
        BlockPos ps = new BlockPos(block.getX(), block.getY(), block.getZ());

        if (isMine(player.getUniqueId(), ps)) return;
        AzPlugin.getInstance().runAsyncLater(()-> {
            PacketHandler.changeBedrock(player, ps);
            put(player.getUniqueId(), ps);
        }, 1);
    }

    public static boolean isMine(UUID uuid, BlockPos pos) {
        if (pos == null) return false;
        if (mineBlocks.containsKey(uuid)) {
            for (BlockPos p : mineBlocks.get(uuid)) {
                if (p.equals(pos)) return true;
            }
        }
        return false;
    }

    private void put(UUID uuid, BlockPos pos) {
        mineBlocks.put(uuid, pos);
        AzPlugin.getInstance().runAsyncLater(()-> {
            mineBlocks.remove(uuid, pos);
            PacketHandler.removeBedrock(Bukkit.getPlayer(uuid), pos);
        }, 200);
    }
}
