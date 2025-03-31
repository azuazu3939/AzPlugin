package com.github.azuazu3939.azPlugin.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerWorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(@NotNull PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        //リセット中ワールドに入れないようにする。
        if (MVWorldListener.isResetWorld(p.getWorld().getName())) {
            p.teleport(e.getFrom().getSpawnLocation());
        }
    }
}
