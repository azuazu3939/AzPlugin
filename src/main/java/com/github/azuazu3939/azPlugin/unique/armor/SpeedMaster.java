package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class SpeedMaster extends Skill {

    private final List<String> list = new ArrayList<>();

    public SpeedMaster() {
        super(new NamespacedKey("az", "speed_master"), 3);
        list.add("§f・スピードに応じて与えるダメージを増加する");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§7§l高速撃";
    }

    public static class System extends SpeedMaster {

        final double multiplier = 0.025;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public double apply(double damage) {
            if (player.hasPotionEffect(PotionEffectType.SLOWNESS)) return damage;

            int i = getLevel(player);
            if (i == 0) return damage;

            return damage * (1 + multiple(getSpeed()) * multiplier);
        }

        private double getSpeed() {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) return 0;
            PotionEffect effect = player.getPotionEffect(PotionEffectType.SPEED);
            if (effect == null) return 0;
            return effect.getAmplifier() + 1;
        }

        private double multiple(double i) {
            if (i == 0) return 0;
            return Math.pow(2, (i - 1));
        }
    }
}
