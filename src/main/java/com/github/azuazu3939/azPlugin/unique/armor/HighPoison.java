package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HighPoison extends Skill {

    private final List<String> list = new ArrayList<>();

    public HighPoison() {
        super(new NamespacedKey("az", "high_poison"), 5);
        list.add("§f・攻撃を充てることで敵を毒状態にする。");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§5§lハイポイズン";
    }

    public static class System extends HighPoison {

        final int tick = 1200;
        final int chance = 10;

        private final Player player;

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        public System(Player player) {
            this.player = player;
        }

        public void apply(LivingEntity entity) {
            if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return;
            Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 20);

            int i = getLevel(player);
            if (i == 0) return;

            if (getRandom().nextInt(100) < chance) {
                player.sendActionBar(Component.text("ハイポイズンが発動しました"));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.POISON, tick, i, true, false, true));
            }
        }
    }
}
