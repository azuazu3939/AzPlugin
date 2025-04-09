package com.github.azuazu3939.azPlugin.gimmick.actions;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockBreakAction;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.minecraft.core.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class BlockAction {

    private static final Multimap<UUID, BlockPos> packetBlocks = ArrayListMultimap.create();
    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

    public static void databaseLocation(@NotNull Player player, BlockPos pos) {
        if (Utils.isCoolTime(BlockAction.class, player.getUniqueId(), multimap)) return;
        Utils.setCoolTime(BlockAction.class, player.getUniqueId(), multimap, 1);

        DBCon.AbstractLocationSet set = DBCon.getLocationSet(new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ()));
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        if (i == 1) {
            BreakAction.breakProcess(player, set);
        } else if (i == 2) {
            InteractAction.interactProcess(player, set);
        } else if (i == 3) {
            PlaceAction.placeProcess(player, set);
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

    protected static void cooldown(@NotNull Player player, BlockPos ps, @NotNull BlockBreakAction action) {
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

    protected static void dropItemStack(Player player, String mmid, @NotNull BlockBreakAction action) {
        org.bukkit.inventory.ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, action.amount());
        Utils.dropItem(player, item);
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    public static void clear(@NotNull Player player) {
        packetBlocks.removeAll(player.getUniqueId());
    }
}
