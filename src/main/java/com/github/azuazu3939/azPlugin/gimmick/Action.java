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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Action {

    private static final Multimap<UUID, BlockPos> packetBlocks = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private static final Multimap<UUID, BlockPos> breakBlocks = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    private static final Multimap<UUID, BlockPos> placeBlocks = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public static void loadBreak(@NotNull Player player, @NotNull BlockPos pos) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (DBCon.locationToInt(set, 1)) {
            doBreak(player, set);
        }
    }

    public static void loadInteract(@NotNull Player player, @NotNull BlockPos pos) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (DBCon.locationToInt(set, 2) ) {
            doInteract(player, set);
        }
    }

    public static boolean loadPlace(@NotNull Player player, @NotNull BlockPos pos, @NotNull ItemStack itemInHand) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return false;
        if (DBCon.locationToInt(set, 3)) {
            return doMultiPlace(player, set, itemInHand);
        }
        return false;
    }

    public static boolean isAffected(UUID uuid, BlockPos pos) {
        if (pos == null) return false;
        return packetBlocks.containsEntry(uuid, pos);
    }

    public static boolean isPlaced(UUID uuid, BlockPos pos) {
        if (pos == null) return false;
        return placeBlocks.containsEntry(uuid, pos);
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
        if (isAffected(player.getUniqueId(), ps) && breakBlocks.containsKey(player.getUniqueId())) {
            cooldown(player, ps, ac);
            return;
        }
        breakBlocks.put(player.getUniqueId(), ps);
        AzPlugin.getInstance().runAsyncLater(() -> breakBlocks.remove(player.getUniqueId(), ps), ac.tick());

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

    protected static boolean doMultiPlace(Player player, @NotNull DBCon.AbstractLocationSet set, ItemStack itemInHand) {
        Optional<BlockPlaceAction> op = DBBlockPlace.getLocationAction(set);
        if (op.isEmpty()) return false;

        BlockPlaceAction ac = op.get();
        if (!materialCheck(itemInHand, ac.material())) return false;
        if (!mythicCheck(itemInHand, ac.mmid())) return false;

        Optional<BlockEditAction> op2 = DBBlockEdit.getBlockEditAction(ac.trigger());
        if (op2.isEmpty()) return false;
        BlockEditAction actions = op2.get();

        Collection<BlockPos> poss = actions.set().stream().map(s -> new BlockPos(s.x(), s.y(), s.z())).collect(Collectors.toSet());
        Collection<Player> list = player.getLocation().getNearbyPlayers(64);

        AzPlugin.getInstance().runAsyncLater(()-> {
            PacketHandler.multiChangeBlock(player, list, poss, ac.material(), ac.tick());
            if (ac.tick() > 0) {
                put(list, poss, ac.tick());
            }
        }, 1);
        return true;
    }

    public static void doPlace(@NotNull Player player, @NotNull Block pos, ItemStack itemInHand) {
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        if (!DBCon.locationToInt(set, 3)) return;

        Optional<BlockPlaceAction> op = DBBlockPlace.getLocationAction(set);
        if (op.isEmpty()) return;

        BlockPlaceAction ac = op.get();
        if (!materialCheck(itemInHand, ac.material())) return;

        Collection<Player> list = player.getLocation().getNearbyPlayers(64);
        Collection<BlockPos> poss = Collections.singleton(new BlockPos(set.x(), set.y(), set.z()));
        AzPlugin.getInstance().runAsyncLater(()-> {
            PacketHandler.multiChangeBlock(player, list, poss, ac.material(), ac.tick());
            if (ac.tick() > 0) {
                put(list, poss, ac.tick());
            }
        }, 2);
    }

    protected static boolean materialCheck(ItemStack itemInHand, Material material) {
        return itemInHand != null && itemInHand.getType() == material;
    }

    protected static boolean mythicCheck(ItemStack itemInHand, String mmid) {
        if (mmid == null) return false;
        return mmid.equals(MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemInHand));
    }

    public static void placed(UUID uuid, BlockPos pos, int tick) {
        placeBlocks.put(uuid, pos);
        AzPlugin.getInstance().runAsyncLater(() -> placeBlocks.remove(uuid, pos), tick);
    }
}
