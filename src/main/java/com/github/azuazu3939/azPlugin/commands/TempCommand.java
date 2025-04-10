package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.gimmick.records.*;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempCommand implements TabExecutor {

    private static final Map<String, BlockDropAction> ACT_DROP = new HashMap<>();
    private static final Map<String, BlockEditAction> ACT_EDIT = new HashMap<>();
    private static final Map<String, BlockInteractAction> ACT_INTERACTION = new HashMap<>();
    private static final Map<String, BlockPlaceAction> ACT_PLACE = new HashMap<>();
    private static final Map<String, BlockBreakAction> ACT_BREAK = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length == 0) {
            player.sendMessage(Component.text("------------------------------------------------------------"));
            player.sendMessage(Component.text("///temp inventory create [<shop_name>]"));
            player.sendMessage(Component.text("///temp inventory open [<shop_name>] <display_name>"));
            player.sendMessage(Component.text("------------------------------------------------------------"));
            player.sendMessage(Component.text("///temp drop create [<drop_name>] <break_material> <break_tick>"));
            player.sendMessage(Component.text("///temp drop add [<drop_name>] [<mmid>] <amount> <chance> "));
            player.sendMessage(Component.text("------------------------------------------------------------"));
            player.sendMessage(Component.text("///temp edit create [<edit_name>] <place_material> <place_tick>"));
            player.sendMessage(Component.text("------------------------------------------------------------"));
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("///temp define <inventory|drop|edit> <name_id> <select_material>"));
            player.sendMessage(Component.text(""));
            player.sendMessage(Component.text("------------------------------------------------------------"));

        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}
