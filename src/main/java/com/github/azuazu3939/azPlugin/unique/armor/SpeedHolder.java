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

public class SpeedHolder extends Skill {

    private final List<String> list = new ArrayList<>();

    public SpeedHolder() {
        super(new NamespacedKey("az", "speed_holder"), 1);
        list.add("§f・スピードに応じて確率で被ダメを無効化する");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§d§l回避術";
    }

    public static class System extends SpeedHolder {

        int value = 5;
        int applyLimitAmplifier = 3;

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public boolean apply() {
            if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return false;
            Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 60);

            if (player.hasPotionEffect(PotionEffectType.SLOWNESS)) return false;
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) return false;

            PotionEffect e = player.getPotionEffect(PotionEffectType.SPEED);
            if (e == null) return false;

            double i = getLevel(player);
            if (i == 0) return false;

            int l = Math.min(e.getAmplifier(), applyLimitAmplifier);
            boolean b = getRandom().nextInt(100) < multiple(l) * value;
            if (b) {
                player.sendActionBar(Component.text("回避術が発動しました"));
            }
            return b;
        }

        private double multiple(double i) {
            return Math.pow(2, (i - 1));
        }
    }
}
