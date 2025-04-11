package com.github.azuazu3939.azPlugin.gimmick;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.*;
import com.github.azuazu3939.azPlugin.gimmick.holder.BaseAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.holder.EmptyAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.records.*;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class Action {

    private static final Multimap<UUID, BlockPos> packetBlocks = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public static void loadBreak(@NotNull Player player, @NotNull BlockPos pos) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (DBCon.locationToInt(set) == 1) {
            doBreak(player, set);
        }
    }

    public static void loadInteract(@NotNull Player player, @NotNull BlockPos pos) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (DBCon.locationToInt(set) == 2) {
            doInteract(player, set);
        }
    }

    public static void loadPlace(@NotNull Player player, @NotNull BlockPos pos, @NotNull ItemStack itemInHand) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (DBCon.locationToInt(set) == 3) {
            doPlace(player, set, itemInHand);
        }
    }

    public static synchronized boolean isAffected(UUID uuid, BlockPos pos) {
        if (pos == null) return false;
        if (packetBlocks.containsKey(uuid)) {
            for (BlockPos p : packetBlocks.get(uuid)) {
                if (p.equals(pos)) return true;
            }
        }
        return false;
    }

    public static void cooldown(@NotNull Player player, BlockPos ps, @NotNull BlockBreakAction action) {
        AzPlugin.getInstance().runAsync(() ->
                PacketHandler.changeBlock(player, ps, action.material()));
    }

    protected static synchronized void put(UUID uuid, BlockPos pos, long tick) {
        packetBlocks.put(uuid, pos);
        if (tick > 0) {
            AzPlugin.getInstance().runAsyncLater(()-> {
                PacketHandler.undoEffected(Bukkit.getPlayer(uuid), pos);
                AzPlugin.getInstance().runAsyncLater(()-> packetBlocks.remove(uuid, pos), 2);
            }, tick);
        }
    }
    protected static void put(@NotNull Collection<Player> ps, @NotNull Collection<BlockPos> poss, long tick) {
        ps.forEach(player ->
                poss.forEach(pos ->
                        put(player.getUniqueId(), pos, tick)));
    }

    protected static void dropItemStack(Player player, String mmid, @NotNull BlockDropAction action) {
        org.bukkit.inventory.ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, action.amount());
        Utils.dropItem(player, item);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    public static synchronized void clear(@NotNull Player player) {
        packetBlocks.removeAll(player.getUniqueId());
    }

    protected static void doBreak(@NotNull Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockBreakAction> st = DBBlockBreak.getLocationAction(set);
        if (st.isEmpty()) return;
        BlockBreakAction ac = st.get();
        Optional<BlockDropAction> op2 = DBBlockDrop.getBlockDropAction(ac.trigger());
        if (op2.isEmpty()) return;

        BlockDropAction action = op2.get();
        BlockPos ps = new BlockPos(set.x(), set.y(), set.z());
        if (isAffected(player.getUniqueId(), ps)) {
            cooldown(player, ps, ac);
            return;
        }

        String mmid = action.mmid();
        Random ran = new Random();
        if (mmid != null && ran.nextDouble() < action.chance()) {
            dropItemStack(player, mmid, action);
        }
        player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1 ,1);

        AzPlugin.getInstance().runAsyncLater(() -> { //戻す処理
            PacketHandler.changeBlock(player, ps, ac.material());
            if (ac.tick() > 0) {
                put(player.getUniqueId(), ps, ac.tick());
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

    protected static void doPlace(Player player, @NotNull DBCon.AbstractLocationSet set, ItemStack itemInHand) {
        Optional<BlockPlaceAction> op = DBBlockPlace.getLocationAction(set);
        if (op.isEmpty()) return;

        BlockPlaceAction ac = op.get();
        if (!materialCheck(itemInHand, ac.material())) return;

        Optional<BlockEditAction> op2 = DBBlockEdit.getBlockEditAction(ac.trigger());
        if (op2.isEmpty()) return;
        BlockEditAction actions = op2.get();

        Set<BlockPos> poss = new HashSet<>();
        for (DBCon.AbstractLocationSet action : actions.set()) {
            BlockPos pos = new BlockPos(action.x(), action.y(), action.z());
            if (isAffected(player.getUniqueId(), pos)) continue;
            poss.add(pos);
        }

        Collection<Player> list = player.getLocation().getNearbyPlayers(64);
        AzPlugin.getInstance().runAsyncLater(()-> {
            PacketHandler.multiChangeBlock(player, list, poss, actions.material());
            if (actions.tick() > 0) {
                put(list, poss, actions.tick());
            }
        }, 1);
    }

    protected static boolean materialCheck(ItemStack itemInHand, Material material) {
        return itemInHand != null && itemInHand.getType() == material;
    }
}
