package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.commands.PositionCommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class GoldenAxeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onGoldenAxe(@NotNull PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.GOLDEN_AXE) return;
        if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND) return;
        if (player.isSneaking()) return;
        if (!player.hasPermission("azplugin.command.pos1")) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (event.getAction().isLeftClick()) {
            PositionCommand.setPos1(player, block.getLocation());
        } else {
            PositionCommand.setPos2(player, block.getLocation());
        }
        event.setCancelled(true);
    }
}
