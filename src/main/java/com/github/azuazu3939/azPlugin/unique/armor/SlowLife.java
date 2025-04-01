package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SlowLife extends Skill {

    private final List<String> list = new ArrayList<>();

    public SlowLife() {
        super(new NamespacedKey("az", "slow_life"), 3);
        list.add("&f・HP回復効果量を上昇させる");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "スローライフ";
    }

    public static class System extends SlowLife {

        final double value = 0.25;

        private static final Multimap<Class<?>, UUID> multimap = HashMultimap.create();

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public double apply(double heal) {
            if (Utils.isCoolTime(getClass(), player.getUniqueId(), multimap)) return heal;
            Utils.setCoolTime(getClass(), player.getUniqueId(), multimap, 10);

            double i = getLevel(player);
            if (i == 0) return heal;

            return heal * (1 + value * multiple(i));
        }

        private double multiple(double i) {
            return Math.pow(2, (i - 1));
        }
    }
}
