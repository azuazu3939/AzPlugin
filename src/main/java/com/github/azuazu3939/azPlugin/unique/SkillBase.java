package com.github.azuazu3939.azPlugin.unique;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public interface SkillBase {

    int getLevel(Player player);

    String getString(int partsLevel, Player player);

    int getAsInt(Player player);

    NamespacedKey getKey();

    Random getRandom();

    List<String> getLore();

    String getName();
}
