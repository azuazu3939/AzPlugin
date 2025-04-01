package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class DungeonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        int i = new Random().nextInt(7) + 1;
        org.bukkit.World w = Bukkit.getWorld("oak_wood_land");
        if (w == null) return false;
        World world = BukkitAdapter.adapt(w);

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);
        if (regionManager == null) return false;

        ProtectedRegion region = regionManager.getRegion("oak_wood_land_dungeon_1_F" + i);
        if (region == null) return false;

        Location l = region.getFlag(Flags.TELE_LOC);
        if (l == null) return false;
        player.teleport(BukkitAdapter.adapt(l));
        player.sendMessage(Component.text("§a木のダンジョンへテレポートしました。"));
        AzPlugin.getInstance().runLater(()-> MythicBukkit.inst().getAPIHelper().castSkill(player, "Utils_OakWoodLand_Wall_StrippedOakWood_Spawn_" + i), 20);
        return true;
    }
}
