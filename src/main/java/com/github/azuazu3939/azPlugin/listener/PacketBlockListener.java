package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.commands.ControlCommand;
import com.github.azuazu3939.azPlugin.database.DBBlockInventory;
import com.github.azuazu3939.azPlugin.gimmick.Action;
import com.github.azuazu3939.azPlugin.gimmick.holder.RegisterAzHolder;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
            ControlCommand.putShop(holder.getShopId(), holder.getInventory());
            e.getPlayer().sendMessage(Component.text("///temp inventory open " + holder.getShopId() + " で、編集できます"));
            DBBlockInventory.updateBlockInteractAsync(holder.getShopId(), inv, e.getView().getCursor());
        }
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());

        Action.loadBreak(player, pos);
    }

    @EventHandler
    public void onPlace(@NotNull BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
        place(player, event.getBlockAgainst(), itemInHand);
        if (place(player, event.getBlockPlaced(), itemInHand)) {
            Action.doPlace(player, event.getBlockPlaced(), itemInHand);
        }
    }

    private boolean place(Player player, @NotNull Block block, ItemStack itemInHand) {
        BlockPos pos = new BlockPos(block.getX(), block.getY(), block.getZ());
        return Action.loadPlace(player, pos, itemInHand);
    }

    @EventHandler
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getInteractionPoint();
        if (loc == null) return;

        Block place = loc.getBlock().getRelative(event.getBlockFace());
        BlockPos pos = new BlockPos(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        BlockPos placePos = new BlockPos(place.getX(), place.getY(), place.getZ());

        Action.loadInteract(player, pos);
        Action.loadInteract(player, placePos);
    }
}
