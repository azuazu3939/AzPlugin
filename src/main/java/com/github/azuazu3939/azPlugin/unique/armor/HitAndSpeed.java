package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HitAndSpeed extends Skill {

    private final List<String> list = new ArrayList<>();

    public HitAndSpeed() {
        super(new NamespacedKey("az", "hit_and_speed"), 4);
        list.add("§f・攻撃時に自身のスピードを上昇させる");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§a§lボルテージ";
    }

    public static class System extends HitAndSpeed {

        int tick = 300;

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public void apply() {
            if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return;
            Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 5);

            int i = getLevel(player);
            if (i == 0) return;

            int now = -1;
            if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                PotionEffect e = player.getPotionEffect(PotionEffectType.SPEED);
                if (e == null) return;
                now = e.getAmplifier();
            }

            int check = Math.min(now + 1, i - 1);
            player.sendActionBar(Component.text("ボルテージが発動しました"));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, tick, check, true, false, true));
        }
    }
}
