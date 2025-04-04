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
            FINAL_DAMAGE *= getValue(TOUGHNESS);
        }
    }

    public double getFinalDamage() {
        return Math.max(1, Math.round(FINAL_DAMAGE / 20));
    }

    private double math(double lim, double value) {
        double a = Math.min(value, lim);
        return 1 - a  / (a + lim);
    }

    private double getValue(double value) {
        double resistance = 1.0;
        for (int depth = 0; depth < 10; depth++) {

            double lim = 100 * Math.pow(2, depth);
            resistance *= math(lim, value);

            if (value <= lim) {
                break;
            }
        }
        return resistance;
    }
}
