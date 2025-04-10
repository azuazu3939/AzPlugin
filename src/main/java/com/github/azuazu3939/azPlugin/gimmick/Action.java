package com.github.azuazu3939.azPlugin.gimmick;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.*;
import com.github.azuazu3939.azPlugin.gimmick.holder.BaseAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.holder.EmptyAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockDropAction;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockEditAction;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockInteractAction;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockPlaceAction;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public abstract class Action {

    private static final Multimap<UUID, BlockPos> packetBlocks = ArrayListMultimap.create();

    public static void load(@NotNull Player player, @NotNull BlockPos pos) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        if (i == 1) {
            doBreak(player, set);
        } else if (i == 2) {
            doInteract(player, set);
        } else if (i == 3) {
            doPlace(player, set);
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

    protected static void cooldown(@NotNull Player player, BlockPos ps, @NotNull BlockDropAction action) {
        AzPlugin.getInstance().runAsync(() ->
                PacketHandler.changeBlock(player, ps, action.ct_material()));
    }

    protected static void put(UUID uuid, BlockPos pos, long tick) {
        packetBlocks.put(uuid, pos);
        if (tick > 0) {
            AzPlugin.getInstance().runAsyncLater(()-> {
                packetBlocks.remove(uuid, pos);
                PacketHandler.undoEffected(Bukkit.getPlayer(uuid), pos);
            }, tick);
        }
    }

    protected static void dropItemStack(Player player, String mmid, @NotNull BlockDropAction action) {
        org.bukkit.inventory.ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, action.amount());
        Utils.dropItem(player, item);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    public static void clear(@NotNull Player player) {
        packetBlocks.removeAll(player.getUniqueId());
    }

    protected static void doBreak(@NotNull Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<String> st = DBBlockBreak.getLocationAction(set);
        if (st.isEmpty()) return;
        Optional<BlockDropAction> ac = DBBlockDrop.getBlockDropAction(st.get());
        if (ac.isEmpty()) return;

        BlockDropAction action = ac.get();
        BlockPos ps = new BlockPos(set.x(), set.y(), set.z());
        if (isAffected(player.getUniqueId(), ps)) {
            cooldown(player, ps, action);
            return;
        }

        String mmid = action.mmid();
        Random ran = new Random();
        if (mmid != null && ran.nextDouble() < action.chance()) {
            dropItemStack(player, mmid, action);
        }
        player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1 ,1);

        AzPlugin.getInstance().runAsyncLater(() -> { //戻す処理
            PacketHandler.changeBlock(player, ps, action.ct_material());
            if (action.tick() > 0) {
                put(player.getUniqueId(), ps, action.tick());
            }
        }, 1);
    }

    protected static void doInteract(@NotNull Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<String> op = DBBlockInteract.getLocationAction(set);
        if (op.isEmpty()) return;
        Optional<BlockInteractAction> op2 = DBBlockInventory.getLocationAction(op.get(), new EmptyAzHolder(6, "テスト").getInventory());
        if (op2.isEmpty()) return;
        BlockInteractAction action = op2.get();
        ShowCaseBuilder.create(player, new BaseAzHolder(6, "§b§lショップ§f: " + op.get(), action.inv(), action.cursor()));
    }

    protected static void doPlace(Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockPlaceAction> op = DBBlockPlace.getLocationAction(set);
        if (op.isEmpty()) return;
        BlockPlaceAction base = op.get();
        Optional<BlockEditAction> op2 = DBBlockEdit.getBlockEditAction(base.trigger());
        if (op2.isEmpty()) return;
        BlockEditAction actions = op2.get();

        for (DBCon.AbstractLocationSet action : actions.set()) {
            BlockPos pos = new BlockPos(action.x(), action.y(), action.z());
            if (isAffected(player.getUniqueId(), pos)) continue;

            AzPlugin.getInstance().runAsyncLater(()-> {
                PacketHandler.changeBlock(player, pos, actions.material());
                if (actions.tick() > 0) {
                    put(player.getUniqueId(), pos, actions.tick());
                }
            }, 1);
        }
    }
}
