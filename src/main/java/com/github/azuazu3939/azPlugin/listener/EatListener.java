package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EatListener implements Listener {

    private final AzPlugin plugin;

    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();
    private static final Map<UUID, Integer> countMap = new HashMap<>();

    public EatListener(AzPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegainHealth(@NotNull EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        double playerHealth = extractPlayerHealth(event);
        UUID playerId = p.getUniqueId();

        if (countMap.getOrDefault(playerId, 0) >= 5) {
            return;
        }
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            adjustEventAmount(event, playerHealth, playerId);
            new ManaRegen(p).forceRegen(0.05);
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.EATING) return;
        if (Utils.isCoolTime(getClass(), playerId, multimap)) {
            applyCooldownAndAdjustAmount(event, playerHealth, playerId);
            new ManaRegen(p).forceRegen(0.2);
        }
    }

    private double extractPlayerHealth(@NotNull EntityRegainHealthEvent event) {
        AttributeInstance attr = ((Player) event.getEntity()).getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
        return attr == null ? 1 : attr.getValue();
    }

    private void applyCooldownAndAdjustAmount(@NotNull EntityRegainHealthEvent event, double playerHealth, UUID playerId) {
        Utils.setCoolTime(getClass(), playerId, multimap, 60);
        event.setAmount(event.getAmount() * 0.1 * playerHealth + 2);
        healCount(playerId);
    }

    private void adjustEventAmount(@NotNull EntityRegainHealthEvent event, double playerHealth, UUID playerId) {
        event.setAmount(event.getAmount() * 0.05 * playerHealth + 1);
        healCount(playerId);
    }

    private void healCount(UUID playerId) {
        countMap.merge(playerId, 1, Integer::sum);
        plugin.runAsyncLater(() -> countMap.merge(playerId, -1, Integer::sum), 200);
    }
}
