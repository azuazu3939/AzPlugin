package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Offence extends Skill {

    private final List<String> list = new ArrayList<>();

    public Offence() {
        super(new NamespacedKey("az", "offence"), 5);
        list.add("§f・自身の攻撃力を上げる");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§c§l攻撃力上昇";
    }

    public static class System extends Offence {

        final int multipleLevel = 3;
        final double multiple = 1.01;
        final int value = 3;
        final int add = 2;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public static final NamespacedKey DAMAGE_KEY =  new NamespacedKey("az", "unique-offence-damage");

        public void apply() {
            int i = getLevel(player);
            if (i == 0) return;

            double a = mathAdd(i);
            double m = Math.ceil(mathMultiply(i) * 100) / 100;

            double damage = value(a, m);
            Utils.addAttribute(player, Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(DAMAGE_KEY, damage, AttributeModifier.Operation.ADD_NUMBER));

        }

        private double value(double value, double multiple) {
            AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
            if (inst == null) return 0;
            return inst.getValue() * (1 + multiple) + value;
        }

        private double mathAdd(int level) {
            return (level - 1) * add + value;
        }

        private double mathMultiply(int level) {
            return (level - multipleLevel) <= 0 ? 0 : Math.pow(multiple, Math.pow(2, level - multipleLevel + 1));
        }

        public void unset() {
            Utils.removeAttribute(player, Attribute.GENERIC_ATTACK_DAMAGE, DAMAGE_KEY);
        }
    }
}
