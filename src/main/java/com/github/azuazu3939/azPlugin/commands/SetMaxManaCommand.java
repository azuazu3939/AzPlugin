package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.mana.Mana;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetMaxManaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            return sendMessage(sender, "§c/setmaxmana <MCID> <maxMana>");
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            return sendMessage(sender, "§cプレイヤーが見つかりません。");
        }

        long maxMana;
        try {
            maxMana = parseMaxMana(args[1]);
        } catch (NumberFormatException e) {
            return sendMessage(sender, "§c最大マナが無効です。");
        }

        new Mana(player).setMaxMana(maxMana);
        player.sendMessage(Component.text("§b操作が完了しました。"));
        return true;
    }

    private long parseMaxMana(String maxManaArg) {
        return Long.parseLong(maxManaArg);
    }

    private boolean sendMessage(@NotNull CommandSender sender, String message) {
        sender.sendMessage(Component.text(message));
        return true;
    }
}
