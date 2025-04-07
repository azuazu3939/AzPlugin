package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.lib.AzHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AzCraftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        player.closeInventory();
        player.openInventory(new AzHolder(6, "テスト").getInventory());
        return true;
    }
}
