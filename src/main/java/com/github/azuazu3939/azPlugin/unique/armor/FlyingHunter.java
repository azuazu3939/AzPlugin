package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FlyingHunter extends Skill {

    private final List<String> list = new ArrayList<>();

    public FlyingHunter() {
        super(new NamespacedKey("az", "flying_hunter"), 3);
        list.add("§f・地上にいない敵へのダメージが増加する");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§b§l空中狙い";
    }

    public static class System extends FlyingHunter {

        final double multiplier = 0.025;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public double apply(@NotNull LivingEntity entity, double damage) {
            if (!entity.isOnGround()) return damage;

            int i = getLevel(player);
            if (i == 0) return damage;

            return damage * (1 + multiple(i) * multiplier);
        }

        private double multiple(double i) {
            return Math.pow(2, (i - 1));
        }
    }
}
