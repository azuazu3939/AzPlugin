package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class OpenFieldListener implements Listener {

    private final NamespacedKey step = new NamespacedKey("az", "default-step-height");

    public double stepAmount = 0.5;

    public void setStep(@NotNull Player player) {
        Utils.addAttribute(
                player,
                Attribute.GENERIC_STEP_HEIGHT,
                new AttributeModifier(step, stepAmount, AttributeModifier.Operation.ADD_NUMBER));
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Utils.removeAttribute(player, Attribute.GENERIC_STEP_HEIGHT, step);
        AzPlugin.getInstance().runLater(()-> setStep(player), 10L);
    }

    @EventHandler
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Utils.removeAttribute(player, Attribute.GENERIC_STEP_HEIGHT, step);
        setStep(player);
    }
}
