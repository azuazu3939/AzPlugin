package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MythicDamageListener implements Listener {

    private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCombatRaidBoss(@NotNull MythicDamageEvent e) {
        AbstractEntity ab = e.getTarget();
        if (!isRaidBoss(ab, this.getClass())) return;
        scheduleRaidBossHealthUpdate(ab);
    }

    private boolean isRaidBoss(@NotNull AbstractEntity ab, Class<?> clazz) {
        ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
        if (mob == null || !mob.hasThreatTable()) return false;
        NamespacedKey key = new NamespacedKey(AzPlugin.getInstance(), "raid_boss");
        return ab.getDataContainer().has(key) && !Utils.isCoolTime(clazz, ab.getUniqueId(), ct);
    }

    private void scheduleRaidBossHealthUpdate(AbstractEntity ab) {
        AzPlugin.getInstance().runLater(() -> {
            ActiveMob mob = MythicBukkit.inst().getMobManager().getActiveMob(ab.getUniqueId()).orElse(null);
            if (ab != null && mob != null) {
                updateRaidBossHealth(ab, mob);
            }
        }, 1);
    }

    private void updateRaidBossHealth(AbstractEntity ab, @NotNull ActiveMob mob) {
        Set<Player> players = getNearByPlayers(ab, getPlayerAmount(mob.getThreatTable()));
        double max = mob.getType().getHealth(mob) * getHealthPerPlayer(players.size());
        double now = ab.getHealth() * getHealthPerPlayer(players.size());
        if (ab.getMaxHealth() == max) return;
        double scale = max / ab.getMaxHealth();
        double setHealth = Math.min(now * scale, ab.getMaxHealth() * scale);
        ab.setMaxHealth(max);
        ab.setHealth(setHealth);
    }

    private @NotNull Set<Player> getNearByPlayers(AbstractEntity ab, @NotNull Set<AbstractPlayer> set) {
        Set<Player> players = new HashSet<>();
        for (AbstractPlayer ap : set) {
            Player p = BukkitAdapter.adapt(ap);
            if (!p.getWorld().getName().equals(ab.getWorld().getName())) continue;
            if (!p.isOnline()) continue;
            if (p.getGameMode().equals(GameMode.CREATIVE) || p.getGameMode().equals(GameMode.SPECTATOR)) continue;
            if (ab.getBukkitEntity().getLocation().distance(p.getLocation()) <= 64) {
                players.add(p);
            }
        }
        return players;
    }

    private @NotNull Set<AbstractPlayer> getPlayerAmount(@NotNull ActiveMob.ThreatTable table) {
        Set<AbstractPlayer> players = new HashSet<>();
        for (AbstractEntity entity : table.getAllThreatTargets()) {
            if (!entity.isPlayer()) continue;
            players.add(entity.asPlayer());
        }
        return players;
    }

    private double getHealthPerPlayer(double n) {
        return n - 0.25 * (n - 1);
    }
}
