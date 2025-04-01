package com.github.azuazu3939.azPlugin.unique.armor;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.unique.Skill;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlessingOfTheEarth extends Skill {

    private final List<String> list = new ArrayList<>();

    public BlessingOfTheEarth() {
        super(new NamespacedKey("az", "blessing_of_the_earth"), 1);
        list.add("&f・地面にいるとき僅かな恩恵を受ける");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "&6&l大地の加護";
    }


    public static class System extends BlessingOfTheEarth {


        public static final int tick = 100;

        private static final Map<UUID, Integer> BukkitTasks = new HashMap<>();

        public void apply(Player player) {
            int i = getLevel(player);
            if (i == 0) return;
            if (!player.isOnGround()) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, tick, i - 1, true, false, true));
        }

        public static void addMember(@NotNull Player player) {
            BukkitTask t = AzPlugin.getInstance().runAsyncTimer(() ->
                    new BlessingOfTheEarth.System().apply(player), System.tick, System.tick);
            BukkitTasks.put(player.getUniqueId(), t.getTaskId());
        }

        public static void removeMember(@NotNull UUID uuid) {
            if (BukkitTasks.containsKey(uuid)) {
                Bukkit.getScheduler().cancelTask(
                        BukkitTasks.get(uuid)
                );
            }
            BukkitTasks.remove(uuid);
        }
    }
}