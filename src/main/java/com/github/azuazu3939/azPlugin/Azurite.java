package com.github.azuazu3939.azPlugin;

import com.github.azuazu3939.azPlugin.database.*;
import com.github.azuazu3939.azPlugin.listener.MythicListener;
import com.github.azuazu3939.azPlugin.mana.Mana;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.unique.armor.Defence;
import com.github.azuazu3939.azPlugin.unique.armor.GroundReactionForce;
import com.github.azuazu3939.azPlugin.unique.armor.Offence;
import com.github.azuazu3939.azPlugin.util.Utils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.azisaba.loreeditor.api.event.EventBus;
import net.azisaba.loreeditor.api.event.ItemEvent;
import net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public final class Azurite {

    public Azurite(AzPlugin plugin) {
        EventBus.INSTANCE.register(plugin, ItemEvent.class, 0, e -> {
            unique(e);
            mana(e);
            weapon(e);
        });

        try {
            DBCon.init();
            DBCon.loadBreak();
            DBCon.loadInteract();
            DBCon.loadPlace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        plugin.runAsyncTimer(()-> {
            DBBlockInteract.clear();
            DBBlockBreak.clear();
            DBBlockInventory.clear();
            DBBlockPlace.clear();
            DBBlockDrop.clear();
            DBBlockEdit.clear();
        }, 36000L, 36000L);

        Bukkit.getOnlinePlayers().forEach(player -> {
            new ManaRegen(player).start();
            GroundReactionForce.System.addMember(player);

            new Defence.System(player).apply();
            new Offence.System(player).apply();
            PacketHandler.inject(player);
        });

        if (MythicListener.isMythic()) {
            MythicListener.reloadMythic(20);
        }
    }

    private void weapon(@NotNull ItemEvent e) {
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(e.getBukkitItem());
        MythicItem mi = (mmid == null) ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        String group = (mi == null) ? null : mi.getGroup();
        if (group == null) return;
        e.addLore(Component.text(""));
        e.addLore(Component.text("§fカテゴリー: §7" + group));
    }

    private void unique(@NotNull ItemEvent e) {
        ItemStack item = e.getBukkitItem();
        Player p = e.getPlayer();
        Skill.getSkills(item).forEach(s -> {
            int i = Skill.getUniqueLevel(s.getKey(), item);
            if (i == -1) return;
            e.addLore(Component.text(""));
            e.addLore(Component.text(s.getName() + " " + s.getString(i, p)));
            s.getLore().forEach(l -> e.addLore(Component.text(l)));
        });
    }

    private void mana(@NotNull ItemEvent e) {
        String s1 = Utils.getItemDataContainerString(e.getBukkitItem(), Mana.MAX_MANA, PersistentDataType.STRING);
        double mana = (s1 == null) ? 0 : Double.parseDouble(s1);

        String s2 = Utils.getItemDataContainerString(e.getBukkitItem(), ManaRegen.MANA_REGEN, PersistentDataType.STRING);
        double manaRegen = (s2 == null) ? 0 : Double.parseDouble(s2);

        if (mana <= 0  && manaRegen <= 0) return;
        e.addLore(Component.text(""));
        e.addLore(Component.text("§7装備したとき："));

        if (mana > 0) {
            e.addLore(Component.text("§bマナ +" + mana));
        }
        if (manaRegen > 0) {
            e.addLore(Component.text("§dマナ回復 +" + manaRegen + "§f/§d1s"));
        }
    }

}
