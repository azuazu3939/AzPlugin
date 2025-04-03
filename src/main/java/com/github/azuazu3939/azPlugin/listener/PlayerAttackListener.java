package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerAttackListener implements Listener {

    private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPre(@NotNull PrePlayerAttackEntityEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSwing(@NotNull PlayerArmSwingEvent event) {
        if (event.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) return;
        Player player = event.getPlayer();

        Skill s = MythicBukkit.inst().getSkillManager().getSkill("Generic_Weapon_Attack_MCLove32").orElse(null);
        if (s == null) return;

        if (player.getAttackCooldown() != 1) return;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(itemStack);
        MythicItem mi = (mmid == null) ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        if (mi == null) return;

        if (mi.getGroup() == null) return;
        if (!mi.getGroup().equals("Melee-Weapon")) return;

        if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return;
        Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 4);

        MythicBukkit.inst().getAPIHelper().castSkill(player, "Generic_Weapon_Attack_MCLove32");
    }
}
