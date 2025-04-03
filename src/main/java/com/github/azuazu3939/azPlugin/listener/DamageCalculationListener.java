package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.lib.Calculation;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class DamageCalculationListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(@NotNull MythicDamageEvent event) {
        AbstractEntity attacker = event.getCaster().getEntity();
        AbstractEntity victim = event.getTarget();

        double baseDamage = event.getDamage();
        double armor = victim.getArmor();
        double toughness = victim.getArmorToughness();
        double attack = attacker.getDamage();

        Calculation c = new Calculation(baseDamage, attack, armor, toughness);
        event.setDamage(c.getFinalDamage());
    }

}
