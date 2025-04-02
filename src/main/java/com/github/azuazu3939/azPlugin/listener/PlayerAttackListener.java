package com.github.azuazu3939.azPlugin.listener;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlayerAttackListener implements Listener {

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPre(@NotNull PrePlayerAttackEntityEvent event) {
        event.setCancelled(true);
    }
}
