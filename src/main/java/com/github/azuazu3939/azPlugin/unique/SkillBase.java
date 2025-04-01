package com.github.azuazu3939.azPlugin.unique;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public interface SkillBase {

    int getLevel(Player player);

    String getString(int partsLevel, Player player);

    int getAsInt(Player player);

    NamespacedKey getKey();

    Set<EquipmentSlot> getSlots();

    Random getRandom();

    @Nullable
    String getData(ItemStack stack, NamespacedKey key);

    List<String> getLore();

    String getName();
}
