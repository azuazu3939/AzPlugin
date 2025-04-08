package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.lib.DamageColor;
import com.github.azuazu3939.azPlugin.lib.packet.PacketHandler;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;

public class MythicDisplayListener implements Listener {


    private static final Random RANDOM = new Random();
    private static final int DISPLAY_DURATION_TICKS = 30;
    private static final int RANDOM_BOUND = 5;
    private static final double RANDOM_SCALE = 0.5;
    private static final double BASE_Y_OFFSET = 2;
    private static final String DAMAGE_PREFIX = "§7§l⚔";

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisplay(@NotNull MythicDamageEvent event) {
        AbstractEntity attacker = event.getCaster().getEntity();
        AbstractEntity victim = event.getTarget();

        if (!isDisplayConditionValid(victim)) return;

        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(victim.getUniqueId()).orElse(null);
        if (mob == null) return;

        Player p = attacker.isPlayer() ? (Player) attacker.asPlayer().getBukkitEntity() : null;
        showDamageDisplay(event, p, victim, mob);
    }

    private boolean isDisplayConditionValid(@NotNull AbstractEntity victim) {
        return !victim.isPlayer() && victim.isLiving();
    }

    private void showDamageDisplay(@NotNull MythicDamageEvent event, Player player, @NotNull AbstractEntity victim, @NotNull ActiveMob mob) {
        String element = event.getDamageMetadata().getElement();
        double multiplier = mob.getType().getDamageModifiers().getOrDefault(element, 1.0);
        double damage = formatDamage(event.getDamage() * multiplier);
        Location location = getRandomLocation(victim.getBukkitEntity().getLocation());
        Component component = Component.text(getDamageElement(element) + damage);
        displayDamageText(player, location, component);
    }

    private void displayDamageText(Player player, @NotNull Location location, Component component) {
        int id = RANDOM.nextInt(Integer.MAX_VALUE);
        if (player == null) {
            location.getNearbyPlayers(32).forEach(p ->
                    send(p, location, component, id));
        } else {
            send(player, location, component, id);
        }
    }

    private void send(Player player, @NotNull Location location, Component component, int id) {
        PacketHandler.spawnTextDisplay(player, location.getX(), location.getY(), location.getZ(), id);
        PacketHandler.setTextDisplayMeta(player, id, component);
        AzPlugin.getInstance().runAsyncLater(() -> PacketHandler.removePacketEntity(player, id), DISPLAY_DURATION_TICKS);
    }

    private double formatDamage(double amount) {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        return Double.parseDouble(numberFormat.format(amount).replaceAll(",", ""));
    }

    @NotNull
    private Location getRandomLocation(@NotNull Location location) {
        return location.add(
                RANDOM.nextInt(RANDOM_BOUND) * RANDOM_SCALE - 1,
                RANDOM.nextInt(RANDOM_BOUND + 1) * 0.1 + BASE_Y_OFFSET,
                RANDOM.nextInt(RANDOM_BOUND) * RANDOM_SCALE - 1
        );
    }

    @NotNull
    private String getDamageElement(String element) {
        if (element == null) return DAMAGE_PREFIX;
        return DamageColor.getColors().entrySet().stream()
                .filter(entry -> element.equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst().map(v -> DAMAGE_PREFIX.replaceAll("§7§l", v)).orElse(DAMAGE_PREFIX);
    }
}
