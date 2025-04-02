package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BadStatusHunter extends Skill {

    private final List<String> list = new ArrayList<>();

    public BadStatusHunter() {
        super(new NamespacedKey("az", "bad_status_hunter"), 3);
        list.add("§f敵が状態異常時に与えるダメージを増加する");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§3§l状態異常特攻";
    }

    public static class System extends BadStatusHunter {

        final double multiplier = 0.05;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public double apply(LivingEntity entity, double damage) {
            int i = getLevel(player);
            if (i == 0) return damage;

            if (!isBad(entity)) return damage;
            return damage * (1 + multiplier * multiple(i));
        }

        private double multiple(double i) {
            return Math.pow(2, (i - 1));
        }

        private boolean isBad(@NotNull LivingEntity entity) {
            return entity.hasPotionEffect(PotionEffectType.BLINDNESS) ||
                    entity.hasPotionEffect(PotionEffectType.POISON) ||
                    entity.hasPotionEffect(PotionEffectType.WITHER) ||
                    entity.hasPotionEffect(PotionEffectType.DARKNESS) ||
                    entity.hasPotionEffect(PotionEffectType.SLOWNESS) ||
                    entity.hasPotionEffect(PotionEffectType.MINING_FATIGUE) ||
                    entity.hasPotionEffect(PotionEffectType.WEAKNESS);
        }
    }
}
