package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DivineBlessing extends Skill {

    private final List<String> list = new ArrayList<>();

    public DivineBlessing() {
        super(new NamespacedKey("az", "divine_blessing"), 4);
        list.add("§f・被弾時に確率でダメージを軽減する");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§a§l精霊の加護";
    }

    public static class System extends DivineBlessing {

        final int chance = 30;
        final double protect = 0.2;

        private final Player player;

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        public System(Player player) {
            this.player = player;
        }

        public double apply(double damage) {
            if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return damage;
            Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 10);

            int i = getLevel(player);
            if (i == 0) return damage;

            if (getRandom().nextInt(100) < chance) {
                player.sendActionBar(Component.text("精霊の加護が発動しました"));
                return damage * (1 - protect * i);
            }
            return damage;
        }
    }
}
