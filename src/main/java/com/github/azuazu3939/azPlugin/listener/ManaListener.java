package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.event.ManaModifiedEvent;
import com.github.azuazu3939.azPlugin.event.ManaModifyEvent;
import com.github.azuazu3939.azPlugin.mana.Mana;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManaListener implements Listener {

    private final AzPlugin plugin;

    private static final Map<UUID, KeyedBossBar> bossBarMap = new HashMap<>();
    private static final Map<UUID, Integer> visibleCountMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> lastUsedManaMap = new HashMap<>();
    private static final int MAX_VISIBLE_THRESHOLD = 5;
    private static final int INITIAL_DELAY = 20;

    public ManaListener(AzPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new ManaRegen(player).start();
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        new ManaRegen(player).stop();
        removeBossBar(player.getUniqueId());
        removeLastUsedMana(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onManaModified(@NotNull ManaModifiedEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (bossBarMap.containsKey(playerId)) {
            updateBossBar(player);
        } else {
            createBossBar(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onManaModify(@NotNull ManaModifyEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (event.getAdd() - event.getBefore() < 0 && lastUsedManaMap.containsKey(playerId)) {
            lastUsedManaMap.remove(playerId);
            return;
        }
        if (lastUsedManaMap.containsKey(playerId)) return;

        lastUsedManaMap.merge(playerId, 1, Integer::sum);
        lastUsedManaCheck(player, playerId);
    }

    private void lastUsedManaCheck(Player player, UUID playerId) {
        plugin.runAsyncLater(()-> {
            if (!lastUsedManaMap.containsKey(playerId)) return;
            lastUsedManaMap.merge(playerId, 1, Integer::sum);

            lastUsedManaRegen(player, playerId);
            lastUsedManaCheck(player, playerId);
        }, 10);
    }

    private void lastUsedManaRegen(Player player, UUID playerId) {
        if (!lastUsedManaMap.containsKey(playerId)) return;
        if (lastUsedManaMap.get(playerId) < 16) return;

        new ManaRegen(player).forceRegen(0.05);
    }

    public static void removeLastUsedMana(UUID playerId) {
        lastUsedManaMap.remove(playerId);
    }

    private void updateBossBar(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        KeyedBossBar bar = bossBarMap.get(playerId);
        bar.setTitle(createBossBarTitle(player));
        bar.setProgress(getProgress(player));
        bar.setVisible(true);
        visibleCountMap.merge(playerId, 3, Integer::sum);
    }

    private double getProgress(@NotNull Player player) {
        Mana mana = new Mana(player);
        return mana.getMana() / mana.getMaxMana();
    }

    public void createBossBar(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        NamespacedKey key = new Key(plugin).getOrCreate(playerId.toString().toLowerCase());
        KeyedBossBar keyedBossBar = Bukkit.createBossBar(key, createBossBarTitle(player), BarColor.BLUE, BarStyle.SEGMENTED_10);
        barFlags(keyedBossBar, player);
        bossBarMap.put(playerId, keyedBossBar);
        visibleCountMap.put(playerId, 3);
        subtractVisibleCount(playerId);
    }

    private void barFlags(@NotNull KeyedBossBar keyedBossBar, @NotNull Player player) {
        keyedBossBar.setProgress(getProgress(player));
        keyedBossBar.addPlayer(player);
        keyedBossBar.setVisible(true);
    }

    @NotNull
    private String createBossBarTitle(@NotNull Player player) {
        Mana mana = new Mana(player);
        NumberFormat num = NumberFormat.getInstance();
        num.setMaximumFractionDigits(0);
        return "§b§lマナ §f§l" + num.format(mana.getMana())+ " §f/§f§l " + num.format(mana.getMaxMana());
    }

    private void subtractVisibleCount(UUID playerId) {
        plugin.runAsyncLater(() -> {
            decrementVisibleCount(playerId);
            ensureVisibleCountWithinLimits(playerId);
            handleBossBarVisibility(playerId);
        }, INITIAL_DELAY);
    }

    private void decrementVisibleCount(UUID playerId) {
        visibleCountMap.merge(playerId, -1, Integer::sum);
    }

    private void ensureVisibleCountWithinLimits(UUID playerId) {
        visibleCountMap.computeIfPresent(playerId, (uuid, count) -> Math.min(count, MAX_VISIBLE_THRESHOLD));
    }

    private void handleBossBarVisibility(UUID playerId) {
        if (getVisibleCount(playerId) <= 0) {
            removeBossBar(playerId);
        } else {
            rescheduleVisibilityCheck(playerId);
        }
    }

    private int getVisibleCount(UUID playerId) {
        return visibleCountMap.getOrDefault(playerId, 0);
    }

    private void rescheduleVisibilityCheck(UUID playerId) {
        if (getVisibleCount(playerId) > MAX_VISIBLE_THRESHOLD) {
            visibleCountMap.put(playerId, MAX_VISIBLE_THRESHOLD);
        }
        subtractVisibleCount(playerId);
    }

    public static void removeBossBar(@NotNull UUID playerId) {
        visibleCountMap.remove(playerId);
        if (bossBarMap.containsKey(playerId)) {
            KeyedBossBar bar = bossBarMap.get(playerId);
            bar.setVisible(false);
            bar.removeAll();
            bossBarMap.remove(playerId);
        }
    }

    public static void removeAll() {
        for (KeyedBossBar bar : bossBarMap.values()) {
            bar.setVisible(false);
            bar.removeAll();
        }
        bossBarMap.clear();
        visibleCountMap.clear();
    }
}
