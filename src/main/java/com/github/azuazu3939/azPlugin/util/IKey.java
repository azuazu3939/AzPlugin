package com.github.azuazu3939.azPlugin.util;

import org.bukkit.NamespacedKey;

import java.util.Collection;
import java.util.Set;

public interface IKey {

    NamespacedKey getOrCreate(String value);

    boolean containsKey(String value);

    Collection<NamespacedKey> getKeys();

    Set<String> getValues();
}
