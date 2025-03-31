package com.github.azuazu3939.azPlugin.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void onDamage(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.POISON)) return;
        if (!livingEntity.hasPotionEffect(PotionEffectType.POISON)) return;

        PotionEffect effect = livingEntity.getPotionEffect(PotionEffectType.POISON);
        int level = Objects.requireNonNull(effect).getAmplifier();

        event.setDamage(event.getDamage() + Math.sqrt(level));
    }
}
