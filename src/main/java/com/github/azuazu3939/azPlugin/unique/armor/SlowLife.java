package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SlowLife extends Skill {

    private final List<String> list = new ArrayList<>();

    public SlowLife() {
        super(new NamespacedKey("az", "slow_life"), 3);
        list.add("§f・HP回復効果量を上昇させる");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§e§lスローライフ";
    }

    public static class System extends SlowLife {

        final double value = 0.25;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public double apply(double heal) {
            double i = getLevel(player);
            if (i == 0) return heal;

            player.sendActionBar(Component.text("スローライフが発動しました"));
            return heal * (1 + value * multiple(i));
        }

        private double multiple(double i) {
            return Math.pow(2, (i - 1));
        }
    }
}
