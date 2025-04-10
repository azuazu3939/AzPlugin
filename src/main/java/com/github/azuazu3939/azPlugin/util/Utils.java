package com.github.azuazu3939.azPlugin.util;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.listener.MVWorldListener;
import com.github.azuazu3939.azPlugin.listener.MythicListener;
import com.google.common.collect.Multimap;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Difficulty;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {

    public static void dropItem(@NotNull Player player, @NotNull ItemStack item) {
        AzPlugin.getInstance().run(()->
                player.getInventory().addItem(item).forEach((n, i) ->
                player.getLocation().getWorld().dropItemNaturally(player.getLocation(), item)));
    }

    public static boolean isCoolTime(Class<?> clazz, UUID uuid, @NotNull Multimap<Class<?>, UUID> multimap) {
        return multimap.containsEntry(clazz, uuid);
    }

    public static void setCoolTime(Class<?> clazz, UUID uuid, @NotNull Multimap<Class<?>, UUID> multimap, long tick) {
        multimap.put(clazz, uuid);
        AzPlugin.getInstance().runAsyncLater(() -> multimap.remove(clazz, uuid), tick);
    }

    @Nullable
    public static String getSlotDataContainerString(@NotNull Player p, EquipmentSlot slot, NamespacedKey key, PersistentDataType<String, String> type) {
        return Utils.getItemDataContainerString(p.getInventory().getItem(slot), key, type);
    }

    @Nullable
    public static String getItemDataContainerString(ItemStack item, NamespacedKey key, PersistentDataType<String, String> type) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, type);
    }

    @NotNull
    public static Set<EquipmentSlot> getAllSlots() {
        Set<EquipmentSlot> s = getArmorSlots();
        s.add(EquipmentSlot.HAND);
        s.add(EquipmentSlot.OFF_HAND);
        return s;
    }

    @NotNull
    @Contract(" -> new")
    public static Set<EquipmentSlot> getArmorSlots() {
        return new HashSet<>(Set.of(EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD, EquipmentSlot.CHEST));
    }

    public static void removeAttribute(@NotNull Player player, @NotNull Attribute attribute, NamespacedKey key) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr == null) return;
        if (attr.getModifiers().stream().anyMatch(m -> m.getKey().equals(key))) {
            attr.removeModifier(key);
        }
    }

    public static void addAttribute(@NotNull Player player, @NotNull Attribute attribute, AttributeModifier modifier) {
        AttributeInstance attr = player.getAttribute(attribute);
        if (attr == null) return;
        attr.addModifier(modifier);
    }

    public static double getDataContainerDouble(@NotNull Player p, NamespacedKey key, PersistentDataType<String, String> type, @NotNull Set<EquipmentSlot> slots) {
        AtomicReference<Double> origin = new AtomicReference<>(0.0);
        slots.forEach(slot -> {
            String s = getSlotDataContainerString(p, slot, key, type);
            if (s == null) return;
            origin.updateAndGet(v -> v + Double.parseDouble(s));
        });
        return origin.get();
    }

    public static int getDataContainerInt(@NotNull Player p, NamespacedKey key, PersistentDataType<String, String> type, @NotNull Set<EquipmentSlot> slots) {
        AtomicReference<Integer> origin = new AtomicReference<>(0);
        slots.forEach(slot -> {
            String s = getSlotDataContainerString(p, slot, key, type);
            if (s == null) return;
            origin.updateAndGet(v -> v + Integer.parseInt(s));
        });
        return origin.get();
    }

    public static double getPlayerDataContainerDouble(@NotNull Player p, NamespacedKey key, PersistentDataType<String, String> type, double defaultValue) {
        PersistentDataContainer container = p.getPersistentDataContainer();
        String s = container.get(key, type);
        return s == null ? defaultValue : Double.parseDouble(s);
    }

    public static void createWorld(String name, String gen, Difficulty dif, WorldType type, World.Environment environment, String seed, boolean generate) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        if (!core.getMVWorldManager().addWorld(name, environment, seed, type, generate, gen)) return;

        AzPlugin.getInstance().runLater(() -> applySettings(core, name, dif), 20);
    }

    private static void applySettings(@NotNull MultiverseCore core, String name, Difficulty dif) {
        World w = core.getMVWorldManager().getMVWorld(name).getCBWorld();
        if (w == null) return;
        MVWorldListener.configureWorldSettings(w, dif);

        if (MythicListener.isMythic()) {
            MythicListener.reloadMythic(100);
        }

    }
}
