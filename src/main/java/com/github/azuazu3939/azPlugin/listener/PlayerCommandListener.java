package com.github.azuazu3939.azPlugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onCommand(@NotNull PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("lifenewpve.reload.mythicmobs")) return;
        if (e.getMessage().contains("mm r") || e.getMessage().contains("mythicmobs r")) {
            MythicListener.notifyPlayers();
        }
    }
}
