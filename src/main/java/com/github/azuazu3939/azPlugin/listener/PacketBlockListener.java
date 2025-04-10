package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.commands.CreateShopCommand;
import com.github.azuazu3939.azPlugin.database.DBBlockInventory;
import com.github.azuazu3939.azPlugin.gimmick.Action;
import com.github.azuazu3939.azPlugin.gimmick.holder.RegisterAzHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class PacketBlockListener implements Listener {

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Action.clear(event.getPlayer());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Action.clear(event.getPlayer());
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
