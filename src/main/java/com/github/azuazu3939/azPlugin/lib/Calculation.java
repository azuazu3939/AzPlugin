package com.github.azuazu3939.azPlugin.lib;

public class Calculation {

    protected double ATTACK;
    protected double ARMOR;
    protected double TOUGHNESS;
    protected double FINAL_DAMAGE;
    protected double MOTION;

    public Calculation(double motionValue, double attack, double armor, double toughness) {
        this.ATTACK = Math.max(0, attack);
        this.ARMOR = Math.max(0, armor);
        this.TOUGHNESS = Math.max(0, toughness);
        this.MOTION = Math.max(0, motionValue);

        this.FINAL_DAMAGE = (ATTACK <= 0) ? 0 : Math.max(0, MOTION * ATTACK - ARMOR);
        if (FINAL_DAMAGE > 0) {
            FINAL_DAMAGE *= getValue(200, ARMOR, TOUGHNESS, 1);
            FINAL_DAMAGE *= getValue(100, 0, TOUGHNESS, 3);
        }
    }

    public double getFinalDamage() {
        return FINAL_DAMAGE / 20;
    }

    public Calculation multipleDamage(double m) {
        FINAL_DAMAGE *= m;
        return this;
    }

    private double math(double lim, double armor, double toughness, double scale) {
        double a = Math.min(armor + toughness, lim);
        return 1 - a  / (a + lim * scale);
    }

    private double getValue(int base, double armor, double toughness, double scale) {
        double resistance = 1.0;
        for (int depth = 0; depth < 10; depth++) {

            double lim = base * Math.pow(2, depth);
            resistance *= math(lim, armor, toughness, scale);

            if (armor + toughness <= lim) {
                break;
            }
        }
        return resistance;
    }
}
