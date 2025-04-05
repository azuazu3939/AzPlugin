package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.commands.ModeCommand;
import com.github.azuazu3939.azPlugin.lib.PacketHandler;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GlobalSettingsListener implements Listener {

    private final NamespacedKey step = new NamespacedKey("az", "default-step-height");
    public double stepAmount = 0.5;

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PacketHandler.inject(player);

        Utils.removeAttribute(player, Attribute.GENERIC_STEP_HEIGHT, step);
        AzPlugin.getInstance().runLater(()-> setStep(player), 10L);

        if (player.hasPermission("lifenewpve.command.mode")) {
            ModeCommand.switchMode(player, false);
        }
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PacketHandler.eject(player);
    }


    @EventHandler
    public void onInteractFireWork(@NotNull PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null) return;
        if (item.getType() == Material.FIREWORK_ROCKET) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Utils.removeAttribute(player, Attribute.GENERIC_STEP_HEIGHT, step);
        setStep(player);

        //実質cancel
        if (MVWorldListener.isResetWorld(player.getWorld().getName())) {
            player.teleport(event.getFrom().getSpawnLocation());
        }
    }

    public void setStep(@NotNull Player player) {
        Utils.addAttribute(
                player,
                Attribute.GENERIC_STEP_HEIGHT,
                new AttributeModifier(step, stepAmount, AttributeModifier.Operation.ADD_NUMBER));
    }
}
