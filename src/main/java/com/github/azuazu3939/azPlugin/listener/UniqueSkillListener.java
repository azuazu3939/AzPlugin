package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.unique.armor.*;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.bukkit.events.MythicHealMechanicEvent;
import io.lumine.mythic.bukkit.utils.events.extra.ArmorEquipEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class UniqueSkillListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onProtection(@NotNull MythicDamageEvent event) {
        if (!(event.getTarget().getBukkitEntity() instanceof Player p)) return;
        event.setDamage(new DivineBlessing.System(p).apply(event.getDamage()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHealOnMythic(@NotNull MythicHealMechanicEvent event) {
        if (!(event.getTarget() instanceof Player p)) return;
        event.setHealAmount(new SlowLife.System(p).apply(event.getHealAmount()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHealOnVanilla(@NotNull EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        event.setAmount(new SlowLife.System(p).apply(event.getAmount()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        GroundReactionForce.System.addMember(player);
        new Defence.System(player).unset();
        new Defence.System(player).apply();

        new Offence.System(player).unset();
        new Offence.System(player).apply();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        GroundReactionForce.System.removeMember(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHit(@NotNull MythicDamageEvent event) {
        if (!(event.getCaster().getEntity().getBukkitEntity() instanceof Player p)) return;
        new HitAndSpeed.System(p).apply();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAvoid(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (new SpeedHolder.System(p).apply()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEquip(@NotNull ArmorEquipEvent event) {
        Player player = event.getPlayer();

        GroundReactionForce.System.removeMember(player.getUniqueId());
        GroundReactionForce.System.addMember(player);

        new Defence.System(player).unset();
        new Defence.System(player).apply();

        new Offence.System(player).unset();
        new Offence.System(player).apply();
    }
}
