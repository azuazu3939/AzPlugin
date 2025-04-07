package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.mythicmobs.Fake;
import com.github.azuazu3939.azPlugin.mythicmobs.Placeholder;
import com.github.azuazu3939.azPlugin.mythicmobs.conditions.*;
import com.github.azuazu3939.azPlugin.mythicmobs.mechanics.*;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobItemGenerateEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.core.items.MythicItem;
import net.kyori.adventure.text.Component;
import net.minecraft.core.component.DataComponents;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MythicListener implements Listener {

    @EventHandler
    public void onReload(@NotNull MythicReloadedEvent e) {
        new Placeholder(e.getInstance().getPlaceholderManager()).init();
    }

    public static void notifyPlayers() {
        Bukkit.getOnlinePlayers()
                .forEach(p -> p.sendMessage(Component.text("§a§lMythicMobsのリロードが行われます。")));
    }

    private static void executeReloadCommand(long delay) {
        AzPlugin.getInstance().runLater(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm re -a"), delay);
    }

    public static void reloadMythic(long delay) {
        notifyPlayers();
        executeReloadCommand(delay);
    }

    @EventHandler
    public void onMechanics(@NotNull MythicMechanicLoadEvent event) {
        registerMechanic(event);
    }

    private void registerMechanic(@NotNull MythicMechanicLoadEvent event) {
        String mechanicName = event.getMechanicName().toLowerCase();
        switch (mechanicName) {
            case "setfalldistance", "setfall":
                event.register(new SetFallDistance(event.getConfig()));
                break;
            case "raidboss", "setboss":
                event.register(new RaidBoss());
            case "modifymana", "addmana":
                event.register(new ModifyMana(event.getConfig()));
                break;
            case "setscale", "scale":
                event.register(new SetScale(event.getConfig()));
                break;
            case "fake":
                event.register(new Fake(event.getConfig()));
                break;
            default:
                // 未知の条件には何もしません
                break;
        }
    }

    @EventHandler
    public void onConditions(@NotNull MythicConditionLoadEvent event) {
        registerCondition(event);
    }

    private void registerCondition(@NotNull MythicConditionLoadEvent event) {
        String conditionName = event.getConditionName().toLowerCase();
        switch (conditionName) {
            case "mythicinradius":
                event.register(new MythicInRadius(event.getConfig()));
                break;
            case "fromsurface":
                event.register(new FromSurface(event.getConfig()));
                break;
            case "containregion":
                event.register(new ContainRegion(event.getConfig()));
                break;
            case "hasmana":
                event.register(new HasMana(event.getConfig()));
                break;
            case "worldmatch":
                event.register(new WorldMatch(event.getConfig()));
                break;
            case "canattack":
                event.register(new CanAttack(event.getConfig()));
                break;
            case "weekofday":
                event.register(new WeekOfDay(event.getConfig()));
                break;
            default:
                // 未知の条件には何もしません
                break;
        }
    }

    @EventHandler
    public void onGen(@NotNull MythicMobItemGenerateEvent event) {
        handleItemGen(event);
    }

    private void handleItemGen(@NotNull MythicMobItemGenerateEvent event) {
        MythicItem mi = event.getItem();
        String itemGroup = mi != null ? mi.getGroup() : null;
        if (!"Main-Weapon".equals(itemGroup)) return;

        ItemStack item = event.getItemStack();
        if (item != null && item.hasItemMeta()) {
            net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
            nms.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            event.setItemStack(CraftItemStack.asBukkitCopy(nms));
        }
    }

    public static boolean isMythic() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return plugin != null && plugin.isEnabled();
    }
}
