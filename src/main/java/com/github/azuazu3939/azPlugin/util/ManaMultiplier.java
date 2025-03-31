package com.github.azuazu3939.azPlugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//一秒あたりの回複数
public class ManaMultiplier extends Count {

    private static final Map<UUID, Double> cache = new HashMap<>();
    private final UUID uuid;

    public ManaMultiplier(UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        new ManaMultiplier(uuid, 1.0);
    }

    public ManaMultiplier(UUID uuid, double amount) {
        super(uuid, amount);
        this.uuid = uuid;
        build(uuid, amount);

    }

    public boolean build(UUID uuid, double amount) {
        if (cache.containsKey(uuid)) return false;
        cache.put(uuid, amount);
        return true;
    }

    public double getAmount(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            new ManaMultiplier(uuid);
        }
        return cache.get(uuid);
    }

    public void setAmount(double amount) {
        cache.put(uuid, amount);
    }

    public static class Adapter {

        private static double math(UUID uuid, double skill) {
            if (!cache.containsKey(uuid)) {
                new ManaMultiplier(uuid);
            }
            return cache.get(uuid) * skill;
        }

        public static double getDouble(UUID uuid, double skill) {
            return math(uuid, skill);
        }

        public static float getFloat(UUID uuid, double skill) {
            return (float) math(uuid, skill);
        }

        public static long getLong(UUID uuid, double skill) {
            return (long) math(uuid, skill);
        }

        public static int getInt(UUID uuid, double skill) {
            return (int) math(uuid, skill);
        }
    }
}
