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

public class Defence extends Skill {

    private final List<String> list = new ArrayList<>();

    public Defence() {
        super(new NamespacedKey("az", "defence"), 5);
        list.add("§f・自身の防御力を上げる");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§9§l防御力上昇";
    }

    public static class System extends Defence {

        final int multipleLevel = 3;
        final double multiple = 1.01;
        final int value = 3;
        final int add = 2;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public static final NamespacedKey ARMOR_KEY =  new NamespacedKey("az", "unique-defence-armor");
        public static final NamespacedKey TOUGHNESS_KEY =  new NamespacedKey("az", "unique-defence-toughness");

        public void apply() {
            int i = getLevel(player);
            if (i == 0) return;

            double a = mathAdd(i);
            double m = Math.ceil(mathMultiply(i) * 100) / 100;

            double armor = value(Attribute.GENERIC_ARMOR, a, m);
            double toughness = value(Attribute.GENERIC_ARMOR_TOUGHNESS, a, m);

            add(Attribute.GENERIC_ARMOR, ARMOR_KEY, armor);
            add(Attribute.GENERIC_ARMOR_TOUGHNESS, TOUGHNESS_KEY, toughness);

        }

        private void add(Attribute attr, NamespacedKey key, double value) {
            Utils.addAttribute(player, attr, new AttributeModifier(key, value, AttributeModifier.Operation.ADD_NUMBER));
        }

        private double value(Attribute attr, double value, double multiple) {
            AttributeInstance inst = player.getAttribute(attr);
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
            remove(Attribute.GENERIC_ARMOR, ARMOR_KEY);
            remove(Attribute.GENERIC_ARMOR_TOUGHNESS, TOUGHNESS_KEY);
        }

        private void remove(Attribute attr, NamespacedKey key) {
            Utils.removeAttribute(player, attr, key);
        }
    }
}
