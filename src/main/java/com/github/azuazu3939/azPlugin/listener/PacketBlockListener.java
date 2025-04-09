package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.DBBlockBreak;
import com.github.azuazu3939.azPlugin.database.DBBlockInteract;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.lib.ShowCaseBuilder;
import com.github.azuazu3939.azPlugin.lib.holder.BaseAzHolder;
import com.github.azuazu3939.azPlugin.lib.holder.EmptyAzHolder;
import com.github.azuazu3939.azPlugin.lib.packet.BlockBreakAction;
import com.github.azuazu3939.azPlugin.lib.packet.BlockInteractAction;
import com.github.azuazu3939.azPlugin.lib.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class PacketBlockListener implements Listener {

    private static final Multimap<UUID, BlockPos> packetBlocks = ArrayListMultimap.create();
    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return;
        Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 2);

        DBCon.AbstractLocationSet set = DBCon.getLocationSet(block.getLocation());
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        if (i == 1) {
            breakProcess(player, set);
        } else if (i == 2) {
            interactProcess(player, set);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.getWorld().getName().toLowerCase().contains("open")) return;
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(block.getLocation());
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        if (i == 1) {
            breakProcess(event.getPlayer(), set);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!block.getWorld().getName().toLowerCase().contains("open")) return;
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(block.getLocation());
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        if (i == 1) {
            breakProcess(event.getPlayer(), set);
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        clear(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        clear(event.getPlayer());
    }

    private void breakProcess(@NotNull Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockBreakAction> op = DBBlockBreak.getLocationAction(set);
        if (op.isPresent()) {
            BlockBreakAction action = op.get();
            long tick;
            String mmid = action.mmid();
            Random ran = new Random();

            BlockPos ps = new BlockPos(set.x(), set.y(), set.z());
            if (isAffected(player.getUniqueId(), ps)) {
                cooldown(player, ps, action);
                return;
            }

            if (mmid != null && ran.nextDouble() < action.chance()) {
                dropItemStack(player, mmid, action);
            }

            tick = action.tick();
            player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1 ,1);
            mined(player, ps, action, tick);
        }
    }

    private void interactProcess(@NotNull Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockInteractAction> op = DBBlockInteract.getLocationAction(set, new EmptyAzHolder(6, "テスト").getInventory());
        if (op.isPresent()) {
            BlockInteractAction action = op.get();
            new ShowCaseBuilder(player, new BaseAzHolder(6, "テスト", action.inv(), action.cursor()));
        }
    }

    private void mined(@NotNull Player player, BlockPos ps, BlockBreakAction action, long tick) {
        AzPlugin.getInstance().runAsyncLater(() -> {
            PacketHandler.changeBlock(player, ps, action.ct_material());
            if (tick > 0) {
                put(player.getUniqueId(), ps, tick);
            }
        }, 1);
    }

    private void dropItemStack(Player player, String mmid, @NotNull BlockBreakAction action) {
        ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, action.amount());
        Utils.dropItem(player, item);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    private void cooldown(@NotNull Player player, BlockPos ps, @NotNull BlockBreakAction action) {
        AzPlugin.getInstance().runAsync(() ->
                PacketHandler.changeBlock(player, ps, action.ct_material()));
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
