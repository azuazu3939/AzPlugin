package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
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

        final int value = 3;
        final int add = 2;

        private final Player player;

        public System(Player player) {
            this.player = player;
        }

        public static final NamespacedKey DAMAGE_KEY =  new NamespacedKey("az", "unique-offence-damage");

        public void apply() {
            unset();
            int i = getLevel(player);
            if (i == 0) return;

            double a = mathAdd(i);
            Utils.addAttribute(player, Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(DAMAGE_KEY, a, AttributeModifier.Operation.ADD_NUMBER));
        }

        private double mathAdd(int level) {
            return (level - 1) * add + value;
        }

        public void unset() {
            Utils.removeAttribute(player, Attribute.GENERIC_ATTACK_DAMAGE, DAMAGE_KEY);
        }
    }
}
