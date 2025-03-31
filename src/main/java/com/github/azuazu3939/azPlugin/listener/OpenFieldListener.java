package com.github.azuazu3939.azPlugin.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class OpenFieldListener implements Listener {

    private final NamespacedKey step = new NamespacedKey("rc", "default-step-height");
    private final NamespacedKey speed = new NamespacedKey("rc", "default-add-speed");

    public double stepAmount = 0.5;
    public double MAX_SPEED = 2.0;

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
        Utils.removeAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, speed);

        if (!player.getWorld().getName().equals("Plains")) return;
        AzPlugin.getInstance().runLater(()-> setStep(player), 10L);
    }

    @EventHandler
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Utils.removeAttribute(player, Attribute.GENERIC_STEP_HEIGHT, step);
        Utils.removeAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, speed);

        if (!player.getWorld().getName().equals("Plains")) return;
        setStep(player);
    }

    @EventHandler
    public void onJump(@NotNull PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals("Plains")) return;

        double d = getSpeed(player);
        if (d > MAX_SPEED) return;
        addSpeed(player, d);
    }

    private double getSpeed(@NotNull Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (attr == null) return 0;

        AtomicReference<Double> i = new AtomicReference<>(0.0);
        attr.getModifiers()
                .stream()
                .filter(m -> m.getKey().equals(speed))
                .forEach(m -> i.updateAndGet(v -> v + m.getAmount()));

        return i.get();
    }

    private void addSpeed(@NotNull Player player, double add) {
        Utils.removeAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, speed);
        Utils.addAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(speed, add + 0.1, AttributeModifier.Operation.ADD_SCALAR));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Utils.removeAttribute(player, Attribute.GENERIC_MOVEMENT_SPEED, speed);
    }
}
