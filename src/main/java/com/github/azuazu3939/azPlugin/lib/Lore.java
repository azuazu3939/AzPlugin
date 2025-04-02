package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.mana.Mana;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.azisaba.loreeditor.api.event.EventBus;
import net.azisaba.loreeditor.api.event.ItemEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class Lore {

    private final AzPlugin plugin;

    public Lore(AzPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        EventBus.INSTANCE.register(plugin, ItemEvent.class, 0, e -> {
            uniqueRegister(e);
            manaRegister(e);
            weaponRegister(e);
        });
    }

    private void weaponRegister(@NotNull ItemEvent e) {
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(e.getBukkitItem());
        MythicItem mi = (mmid == null) ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        String group = (mi == null) ? null : mi.getGroup();
        if (group == null) return;
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§fカテゴリー: §7" + group));
    }

    private void uniqueRegister(@NotNull ItemEvent e) {
        ItemStack item = e.getBukkitItem();
        Player p = e.getPlayer();
        Skill.getSkills(item).forEach(s -> {
            int i = Skill.getUniqueLevel(s.getKey(), item);
            if (i == -1) return;
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(s.getName() + " " + s.getString(i, p)));
            s.getLore().forEach(l -> e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(l)));
        });
    }

    private void manaRegister(@NotNull ItemEvent e) {
        String s1 = Utils.getItemDataContainerString(e.getBukkitItem(), Mana.MAX_MANA, PersistentDataType.STRING);
        double mana = (s1 == null) ? 0 : Double.parseDouble(s1);

        String s2 = Utils.getItemDataContainerString(e.getBukkitItem(), ManaRegen.MANA_REGEN, PersistentDataType.STRING);
        double manaRegen = (s2 == null) ? 0 : Double.parseDouble(s2);

        if (mana <= 0  && manaRegen <= 0) return;
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§7装備したとき："));

        if (mana > 0) {
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§bマナ +" + mana));
        }
        if (manaRegen > 0) {
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§dマナ回復 +" + manaRegen + "§f/§d1s"));
        }
    }
}
