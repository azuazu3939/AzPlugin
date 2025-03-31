package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VectorTask {

    @Nullable
    public static Vector getVector(@NotNull Block b, LivingEntity p) {
        if (b.getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return p.getEyeLocation().getDirection().clone().normalize().multiply(3).setY(4);
        } else if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            return p.getEyeLocation().getDirection().clone().normalize().multiply(4).setY(3);
        }
        return null;
    }

    public static void applyVelocityAndRunTask(LivingEntity livingEntity, Player player, Vector vector) {
        if (vector == null) return;
        applyVelocityWithSound(livingEntity, player, vector);

        int i = 0;
        AzPlugin.getInstance().runLater(()-> applyVelocity(livingEntity, player, i), 1);

    }

    public static void applyVelocity(LivingEntity livingEntity, Player player, int i) {
        if (i >= 10) {
            if (livingEntity == null || player == null) return;
            if (livingEntity.isOnGround()) return;
            if (livingEntity.isInLava() || livingEntity.isInWater()) return;
            if (player.isGliding() || player.isFlying()) return;
            if (player.isInLava() || player.isInWater()) return;
        }
        Vector currentVelocity = livingEntity.getVelocity();
        double currentY = currentVelocity.getY();
        livingEntity.setVelocity(currentVelocity.clone().add(player.getEyeLocation().getDirection().clone().multiply(0.1)).multiply(1.05).setY(currentY));
        livingEntity.setFallDistance(0);
        player.setFallDistance(0);
        i++;

        int finalI = i;
        AzPlugin.getInstance().runLater(()-> applyVelocity(livingEntity, player, finalI), 1);
    }

    public static void applyVelocityWithSound(@NotNull LivingEntity livingEntity, @NotNull Player player, Vector vector) {
        livingEntity.setVelocity(vector);
        player.playSound(player, Sound.BLOCK_PISTON_EXTEND, 1, 1);
    }
}
