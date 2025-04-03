package com.github.azuazu3939.azPlugin.listener;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.events.MythicDamageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class DamageCalculationListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(@NotNull MythicDamageEvent event) {
        AbstractEntity attacker = event.getCaster().getEntity();
        AbstractEntity victim = event.getTarget();

        double baseDamage = event.getDamage();
        double armor = victim.getArmor();
        double toughness = victim.getArmorToughness();
        double attack = attacker.getDamage();

        Calculation c = new Calculation(baseDamage, attack, armor, toughness);
        event.setDamage(c.getFinalDamage());
    }

    private static class Calculation {

        protected double ATTACK;
        protected double ARMOR;
        protected double TOUGHNESS;
        protected double FINAL_DAMAGE;

        protected double RESISTANCE = 1.0;

        private Calculation(double baseDamage, double attack, double armor, double toughness) {
            this.ATTACK = attack;
            this.ARMOR = Math.max(0, armor);
            this.TOUGHNESS = Math.max(0, toughness);

            this.FINAL_DAMAGE = (ATTACK <= 0) ? 0 : baseDamage;
            if (FINAL_DAMAGE > 0) {
                int base = 200;
                for (int depth = 0; depth < 10; depth++) {

                    double lim = base * Math.pow(2, depth);
                    RESISTANCE *= math(lim);

                    if (ARMOR + TOUGHNESS <= lim) {
                        break;
                    }
                }
                FINAL_DAMAGE *= RESISTANCE;
            }

            double v = Math.round(FINAL_DAMAGE);
            FINAL_DAMAGE = (v <= 0) ? 0 : v;
        }

        public double getFinalDamage() {
            return FINAL_DAMAGE;
        }

        private double math(double lim) {
            double a = Math.min(ARMOR + TOUGHNESS, lim);
            return 1 - a  / (a + lim);
        }
    }
}
