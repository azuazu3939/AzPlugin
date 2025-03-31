package com.github.azuazu3939.azPlugin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//スキルCD
public class CooldownMultiplier extends Count {

    private static final Map<UUID, Double> cache = new HashMap<>();
    private final UUID uuid;

    public CooldownMultiplier(UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        new CooldownMultiplier(uuid, 1.0);
    }

    protected CooldownMultiplier(UUID uuid, double tick) {
        super(uuid, tick);
        this.uuid = uuid;
        build(uuid, tick);
    }

    public boolean build(UUID uuid, double tick) {
        if (cache.containsKey(uuid)) return false;
        cache.put(uuid, tick);
        return true;
    }

    public double getTick(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            new CooldownMultiplier(uuid);
        }
        return cache.get(uuid);
    }

    public void setTick(double tick) {
        cache.put(uuid, tick);
    }

    public static class Adapter {

        private static double math(UUID uuid, double skill) {
            if (!cache.containsKey(uuid)) {
                new CooldownMultiplier(uuid);
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
