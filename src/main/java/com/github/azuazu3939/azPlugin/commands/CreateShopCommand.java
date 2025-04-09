package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.gimmick.holder.RegisterAzHolder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CreateShopCommand implements TabExecutor {

    private static final Map<String, Inventory> STRING_INVENTORY = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length < 2) {
            player.sendMessage(Component.text("/createShop create [<shopId>] <shopName>"));
            player.sendMessage(Component.text("/createShop open [<shopId>]"));
            return true;
        }
        String arg0 = strings[0];
        if (arg0.equalsIgnoreCase("create")) {

            String key = strings[1];
            Pattern p = Pattern.compile("[a-zA-Z0-9_]");
            if (!p.matcher(key).find()) {
                player.sendMessage(Component.text("shopIdはazAZ09_のみで構成される必要があります。"));
                return true;
            }

            String shopName = "§b§lショップ";
            if (strings.length >= 3) {
                shopName = strings[2].replaceAll("&", "§");
            }
            player.closeInventory();
            Inventory inv = new RegisterAzHolder(6, shopName, key).getInventory();
            player.openInventory(inv);

        } else if (arg0.equalsIgnoreCase("open")) {

            String key = strings[1];
            Pattern p = Pattern.compile("[a-zA-Z0-9_]");
            if (!p.matcher(key).find()) {
                player.sendMessage(Component.text("shopIdはazAZ09_のみで構成される必要があります。"));
                return true;
            }

            if (STRING_INVENTORY.containsKey(key)) {
                Inventory inv = STRING_INVENTORY.get(key);
                player.closeInventory();
                player.openInventory(inv);
                return true;
            }
            player.sendMessage(Component.text("§cそのShopIdは存在しません。"));
            return true;
        }
        player.sendMessage(Component.text("/createShop create [<shopId>] <shopName>"));
        player.sendMessage(Component.text("/createShop open [<shopId>]"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 1) {
            return List.of("create", "open");
        }
        if (strings.length == 2) {
            return List.of("ショップID");
        }
        if (strings.length == 3 && strings[0].equalsIgnoreCase("create")) {
            return List.of("ショップ表示名");
        }
        return List.of();
    }

    public static void putShop(String key, Inventory inv) {
        STRING_INVENTORY.put(key, inv);
    }
}
