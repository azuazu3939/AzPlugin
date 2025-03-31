package com.github.azuazu3939.azPlugin.util;

import com.github.azuazu3939.azPlugin.AzPlugin;
import org.bukkit.NamespacedKey;

import java.util.*;

public class Key implements IKey {

    private final AzPlugin plugin;

    private static final Map<String, NamespacedKey> keys = new HashMap<>();

    public Key(AzPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public NamespacedKey getOrCreate(String value) {
        if (keys.containsKey(value)) {
            return keys.get(value);
        } else {
            NamespacedKey key = new NamespacedKey(plugin, value);
            keys.put(value, key);
            return key;
        }
    }

    @Override
    public boolean containsKey(String value) {
        return keys.containsKey(value);
    }

    @Override
    public Collection<NamespacedKey> getKeys() {
        return keys.values();
    }

    @Override
    public Set<String> getValues() {
        return keys.keySet();
    }
}
