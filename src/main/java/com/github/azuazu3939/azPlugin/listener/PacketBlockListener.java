package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.commands.CreateShopCommand;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.database.DBBlockInventory;
import com.github.azuazu3939.azPlugin.gimmick.actions.BlockAction;
import com.github.azuazu3939.azPlugin.gimmick.actions.BreakAction;
import com.github.azuazu3939.azPlugin.gimmick.actions.PlaceAction;
import com.github.azuazu3939.azPlugin.gimmick.holder.RegisterAzHolder;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PacketBlockListener implements Listener {


    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockAction.databaseLocation(event.getPlayer(), new BlockPos(block.getX(), block.getY(), block.getZ()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(block.getLocation());
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        Player player = event.getPlayer();
        if (i == 1) {
            BreakAction.breakProcess(player, set);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        Block block = event.getBlock();
        DBCon.AbstractLocationSet set = DBCon.getLocationSet(block.getLocation());
        if (set == null) return;
        int i = DBCon.locationToInt(set);
        Player player = event.getPlayer();
        if (i == 1) {
            PlaceAction.placeProcess(player, set);
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        BlockAction.clear(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        BlockAction.clear(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(@NotNull InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof RegisterAzHolder holder) {
            CreateShopCommand.putShop(holder.getShopId(), inv);
            e.getPlayer().sendMessage("Shopの中身を設定しました。//createshop open " + holder.getShopId() + " で、編集できます");
            DBBlockInventory.updateBlockInteractAsync(holder.getShopId(), inv, e.getView().getCursor());
        }
    }
}
