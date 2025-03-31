package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.listener.MVWorldListener;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldSetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return false;
        World world = p.getWorld();
        p.sendMessage(Component.text(world.getName() + "のgameRuleを更新しました。"));
        MVWorldListener.configureWorldSettings(world, world.getDifficulty());
        return true;
    }
}

