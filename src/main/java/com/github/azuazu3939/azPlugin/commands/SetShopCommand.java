package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.util.SetCommandUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class SetShopCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length < 2) {
            player.sendMessage(Component.text("///setShop [<Material>)] [<shop_id>"));
            return true;
        }

        BoundingBox box = PositionCommand.getArea(player);
        if (box == null) {
            player.sendMessage(Component.text("範囲が選択されていません。///pos1、///pos2、または金の斧で範囲を2つ選択してください。"));
            return true;
        }

        Material material;
        try {
            material = Material.valueOf(strings[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("そのマテリアル名は存在しません。");
            return true;
        }

        AzPlugin.getInstance().runAsync(()-> {
            if (player == null) return;
            Set<Location> locations = SetCommandUtil.getLocations(player, box, material);
            int i = 2;
            for (Location loc : locations) {
                i += 2;
            }
            AzPlugin.getInstance().runAsyncLater(()-> player.sendMessage(Component.text("データの書き込みが終了しました。")), i);
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
