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

public class GroundReactionForce extends Skill {

    private final List<String> list = new ArrayList<>();

    public GroundReactionForce() {
        super(new NamespacedKey("az", "ground_reaction_force"), 3);
        list.add("§f・地面にいるときスピード上昇を受ける");
    }

    @Override
    public List<String> getLore() {
        return list;
    }

    @Override
    public String getName() {
        return "§6§l地面反力";
    }


    public static class System extends GroundReactionForce {


        public static final int tick = 400;

        private static final Map<UUID, Integer> BukkitTasks = new HashMap<>();

        public void apply(Player player) {
            int i = getLevel(player);
            if (i == 0) return;
            if (!player.isOnGround()) return;
            Bukkit.getScheduler().runTask(AzPlugin.getInstance(), () ->
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, tick, i - 1, true, false, true)));
        }

        public static void addMember(@NotNull Player player) {
            BukkitTask t = AzPlugin.getInstance().runAsyncTimer(() ->
                    new GroundReactionForce.System().apply(player), 100, 100);
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