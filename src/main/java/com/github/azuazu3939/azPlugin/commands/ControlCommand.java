package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.*;
import com.github.azuazu3939.azPlugin.gimmick.holder.RegisterAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockBreakAction;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockPlaceAction;
import com.github.azuazu3939.azPlugin.util.SetCommandUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class ControlCommand implements TabExecutor {

    private static final Map<String, Inventory> INV_STRING = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length < 1) return help(player);
        String get = strings[0];
        if (get.equalsIgnoreCase("list")) return list(player);

        if (strings.length < 3) return help(player);

        if (get.equalsIgnoreCase("inventory") && strings.length >= 3) return inventory(player, strings);
        if (get.equalsIgnoreCase("edit") && strings.length >= 4) return edit(player, strings);
        if (get.equalsIgnoreCase("drop") && strings.length >= 4) return drop(player, strings);
        if (get.equalsIgnoreCase("define") && strings.length >= 4) return define(player, strings);
        return false;
    }

    @Contract(pure = true)
    private boolean inventory(Player player, @NotNull String[] strings) {
        String arg2 = strings[1];
        String key = strings[2];
        if (arg2.equalsIgnoreCase("create")) {
            if (checkPattern(player, key)) {

                String name = "§b§lショップ";
                if (strings.length == 4) {
                    name = strings[3].replaceAll("&", "§");
                }
                player.closeInventory();
                Inventory inventory = new RegisterAzHolder(6, name, key).getInventory();
                player.openInventory(inventory);
                return true;
            }
        } else if (arg2.equalsIgnoreCase("open")) {
            if (checkPattern(player, key)) {
                if (INV_STRING.containsKey(key)) {
                    Inventory inventory = INV_STRING.get(key);
                    player.closeInventory();
                    player.openInventory(inventory);
                    return true;
                } else {
                    player.sendMessage(Component.text("§cそのShopIdは存在しません。"));
                }
            }
        } else if (arg2.equalsIgnoreCase("delete")) {
            DBBlockInventory.delete(key);
            player.sendMessage(Component.text("データを削除しました。"));
            if (strings.length >= 4) {
                boolean b = false;
                try {
                    b = Boolean.parseBoolean(strings[3]);
                } catch (Exception ignored) {}

                if (b) {
                    DBBlockInteract.delete(key);
                    player.sendMessage(Component.text("座標データを削除しました。"));
                }
            }
            return true;
        }
        return false;
    }

    @Contract(pure = true)
    private boolean edit(Player player, @NotNull String[] strings) {
        String arg2 = strings[1];
        String key = strings[2];
        String place = strings[3];
        if (arg2.equalsIgnoreCase("create")) {
            if (checkPattern(player, key)) {

                BoundingBox box = PositionCommand.getArea(player);
                if (box == null) notBoxes(player);

                Material material;
                try {
                    material = Material.valueOf(place.toUpperCase());
                    if (!material.isBlock()) {
                        player.sendMessage(Component.text("そのマテリアルは設置不可です。"));
                        return false;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("そのマテリアル名は存在しません。");
                    return false;
                }

                int tick = 200;
                if (strings.length >= 5) {
                    try {
                        tick = Integer.parseInt(strings[4]);
                    } catch (NumberFormatException ignored) {}
                }
                DBBlockEdit.updateLocationAsync(SetCommandUtil.getLocations(player, box), tick, material, key);
                player.sendMessage(Component.text("データの書き込みが終了しました。"));
                return true;
            }
        } else if (arg2.equalsIgnoreCase("delete")) {
            DBBlockEdit.delete(key);
            player.sendMessage(Component.text("データを削除しました。"));
            if (strings.length >= 4) {
                boolean b = false;
                try {
                    b = Boolean.parseBoolean(strings[3]);
                } catch (Exception ignored) {}

                if (b) {
                    DBBlockPlace.delete(key);
                    player.sendMessage(Component.text("座標データを削除しました。"));
                }
            }
        }
        return false;
    }

    @Contract(pure = true)
    private boolean drop(Player player, @NotNull String[] strings) {
        String arg2 = strings[1];
        String key = strings[2];
        String value = strings[3];
        if (arg2.equalsIgnoreCase("create")) {
            if (checkPattern(player, key)) {

               MythicItem item = MythicBukkit.inst().getItemManager().getItem(value).orElse(null);
               if (item == null) {
                   player.sendMessage(Component.text("そのmmidは存在しません。"));
                   return true;
               }

               int amount = 1;
               if (strings.length >= 5) {
                   try {
                       amount = Integer.parseInt(strings[4]);
                   } catch (NumberFormatException ignored) {}
               }

               double chance = 1;
               if (strings.length >= 6) {
                   try {
                       chance = Double.parseDouble(strings[5]);
                   } catch (NumberFormatException ignored) {}
               }

               DBBlockDrop.updateLocationAsync(key, value, amount, chance);
               player.sendMessage(Component.text("データの書き込みが終了しました。"));
               return true;
            }
        } else if (arg2.equalsIgnoreCase("delete")) {
            DBBlockDrop.delete(key);
            player.sendMessage(Component.text("データを削除しました。"));
            if (strings.length >= 4) {
                boolean b = false;
                try {
                    b = Boolean.parseBoolean(strings[3]);
                } catch (Exception ignored) {
                }

                if (b) {
                    DBBlockBreak.delete(key);
                    player.sendMessage(Component.text("座標データを削除しました。"));
                }
            }
        }
        return false;
    }

    @Contract(pure = true)
    private boolean define(Player player, @NotNull String[] strings) {
        String type = strings[1];
        String id = strings[2];
        String mat = strings[3];

        BoundingBox box = PositionCommand.getArea(player);
        if (box == null) notBoxes(player);

        Material material;
        try {
            material = Material.valueOf(mat.toUpperCase());
            if (!material.isBlock()) {
                player.sendMessage(Component.text("そのマテリアルは設置不可です。"));
                return false;
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("そのマテリアル名は存在しません。");
            return false;
        }

        int tick = 200;
        if (strings.length >= 5) {
            try {
                tick = Integer.parseInt(strings[4]);
            } catch (NumberFormatException ignored) {}
        }

        Material m = Material.BEDROCK;
        if (strings.length >= 6) {
            try {
                m = Material.valueOf(strings[5].toUpperCase());
                if (!m.isBlock()) {
                    player.sendMessage(Component.text("そのマテリアルは設置不可です。"));
                    return false;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        String mmid = strings.length >= 7 ? strings[6] : null;
        ItemStack i = MythicBukkit.inst().getItemManager().getItemStack(mmid);
        if (i != null && !i.getType().isBlock()) {
            player.sendMessage(Component.text("そのmmidのマテリアルはブロックではありません。"));
            return false;
        }

        if (checkPattern(player, id)) {
            if (type.equalsIgnoreCase("inventory")) return writeInteract(player, id, box, material);
            if (type.equalsIgnoreCase("edit")) return writePlace(player, id, box, material, tick, m, mmid);
            if (type.equalsIgnoreCase("drop")) return writeBreak(player, id, box, material, tick, m);
        }
        return false;
    }

    private boolean list(@NotNull Player player) {
        DBCon.getLocationSet().forEach((key, value) -> {

            if (value == 1) {
                Optional<BlockBreakAction> st = DBBlockBreak.getLocationAction(key);
                if (st.isEmpty()) return;
                player.sendMessage(Component.text("Break-Drop: " + st.get().trigger()));
            }

            if (value == 2) {
                Optional<String> op2 = DBBlockInteract.getLocationAction(key);
                if (op2.isEmpty()) return;
                player.sendMessage(Component.text("Interact-Inventory: " + op2.get()));
            }

            if (value == 3) {
                Optional<BlockPlaceAction> st = DBBlockPlace.getLocationAction(key);
                if (st.isEmpty()) return;
                player.sendMessage(Component.text("Place-Edit: " + st.get().trigger()));
            }
        });
        player.sendMessage(Component.text("§72回実行していない場合は、結果が不十分な可能性があります。(3回以上は意味がない)"));
        return true;
    }

    private boolean checkPattern(Player player, String string) {
        Pattern p = Pattern.compile("[a-zA-Z0-9_]");
        boolean result = p.matcher(string).find();
        if (result) return true;
        player.sendMessage("nameや、idはは、 a-zA-Z0-9_ で構成される必要があります。");
        return false;
    }

    private boolean notBoxes(@NotNull Player player) {
        player.sendMessage(Component.text("範囲が選択されていません。///pos1、///pos2、または金の斧で範囲を2つ選択してください。"));
        return true;
    }

    private boolean help(@NotNull Player player) {
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text("///ctrl list"));
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text("///ctrl inventory create [<shop_name>] <display_name>"));
        player.sendMessage(Component.text("///ctrl inventory open [<shop_name>]"));
        player.sendMessage(Component.text("///ctrl inventory delete [<shop_name>] <all_delete>"));
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text("///ctrl drop create [<drop_name>] [<mmid>] <amount> <chance>"));
        player.sendMessage(Component.text("///ctrl drop delete [<drop_name>] <all_delete>"));
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text("///ctrl edit create [<edit_name>] [<place_material>] <place_tick>"));
        player.sendMessage(Component.text("///ctrl edit delete [<edit_name>] <all_delete>"));
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("///ctrl define inventory [<shop_name>] [<filter_material>]"));
        player.sendMessage(Component.text("///ctrl define drop [<drop_name>] [<filter_material>] <tick> <material>"));
        player.sendMessage(Component.text("///ctrl define edit [<edit_name>] [<filter_material>] <tick> <material> <check_mmid>"));
        player.sendMessage(Component.text(""));
        player.sendMessage(Component.text("------------------------------------------------------------"));
        player.sendMessage(Component.text("**defineは、///pos1、///pos2でエリアを選択しておく必要があります**"));
        player.sendMessage(Component.text("**editのcreateは、defineとは別で、///pos1、///pos2でエリアを選択しておく必要があります**"));
        player.sendMessage(Component.text("**all_deleteはtrue/falseで、デフォルトはfalseです**"));
        return true;
    }

    public static void putShop(String shopId, Inventory inv) {
        INV_STRING.put(shopId, inv);
    }

    private boolean writeInteract(Player player, String key, BoundingBox box, Material material) {
        AzPlugin.getInstance().runAsync(() -> {
            if (player == null) return;
            int i = 1;
            for (DBCon.AbstractLocationSet set : SetCommandUtil.getLocations(player, box, material)) {
                AzPlugin.getInstance().runAsyncLater(() -> DBBlockInteract.updateBlockInteractAsync(set, key), i);
                i++;
            }
            AzPlugin.getInstance().runAsyncLater(()-> player.sendMessage(Component.text("データの書き込みが終了しました。")), i);
        });
        return true;
    }

    private boolean writePlace(Player player, String key, BoundingBox box, Material material, int tick, Material m, String mmid) {
        AzPlugin.getInstance().runAsync(() -> {
            if (player == null) return;
            int i = 1;
            for (DBCon.AbstractLocationSet set : SetCommandUtil.getLocations(player, box, material)) {
                AzPlugin.getInstance().runAsyncLater(()-> DBBlockPlace.updateLocationAsync(set, key, tick, m, mmid), i);
                i++;
            }
            AzPlugin.getInstance().runAsyncLater(()-> player.sendMessage(Component.text("データの書き込みが終了しました。")), i);
        });
        return true;
    }

    private boolean writeBreak(Player player, String key, BoundingBox box, Material material, int tick, Material m) {
        AzPlugin.getInstance().runAsync(() -> {
            if (player == null) return;
            int i = 1;
            for (DBCon.AbstractLocationSet set : SetCommandUtil.getLocations(player, box, material)) {
                AzPlugin.getInstance().runAsyncLater(()-> DBBlockBreak.updateLocationAsync(set, key, tick, m), i);
                i++;
            }
            AzPlugin.getInstance().runAsyncLater(()-> player.sendMessage(Component.text("データの書き込みが終了しました。")), i);
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 1) {
            return List.of("inventory", "drop", "edit", "define", "list");
        }
        if (strings.length == 2) {
            String write = strings[0];
            if (write.equalsIgnoreCase("inventory")) return List.of("create", "open", "delete");
            if (write.equalsIgnoreCase("drop")) return List.of("create", "delete");
            if (write.equalsIgnoreCase("edit")) return List.of("create", "delete");
            if (write.equalsIgnoreCase("define")) return List.of("inventory", "drop", "edit");
        }
        if (strings.length == 3) {
            String type = strings[0];
            if (type.equalsIgnoreCase("inventory")) return DBBlockInventory.get().stream().toList();
            if (type.equalsIgnoreCase("drop")) return DBBlockDrop.get().stream().toList();
            if (type.equalsIgnoreCase("edit")) return DBBlockEdit.get().stream().toList();
            if (type.equalsIgnoreCase("define")) {
                String string = strings[1];
                if (string.equalsIgnoreCase("inventory")) return DBBlockInventory.get().stream().toList();
                if (string.equalsIgnoreCase("drop")) return DBBlockDrop.get().stream().toList();
                if (string.equalsIgnoreCase("edit")) return DBBlockEdit.get().stream().toList();
            }
        }

        if (strings.length == 4) {
            String write = strings[0];
            String type = strings[1];
            if (write.equalsIgnoreCase("drop") && type.equalsIgnoreCase("create")) {
                String ss = strings[3];
                if (ss.isBlank() || ss.isEmpty()) {
                    return MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList();
                } else {
                    List<String> sort = new ArrayList<>();
                    new ArrayList<>(MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList())
                            .stream().filter(m -> m.toUpperCase().contains(ss.toUpperCase()))
                            .forEach(sort::add);
                    return sort;
                }
            }
            if (write.equalsIgnoreCase("edit") && type.equalsIgnoreCase("create")) {
                String ss = strings[3];
                if (ss.isBlank() || ss.isEmpty()) {
                    return Arrays.stream(Material.values()).map(Enum::toString).toList();
                } else {
                    List<String> sort = new ArrayList<>();
                    new ArrayList<>(Arrays.stream(Material.values()).map(Enum::name).toList())
                            .stream().filter(m -> m.contains(ss.toUpperCase()))
                            .forEach(sort::add);
                    return sort;
                }
            }
            if (write.equalsIgnoreCase("define")) {
                String ss = strings[3];
                if (ss.isBlank() || ss.isEmpty()) {
                    return Arrays.stream(Material.values()).map(Enum::toString).toList();
                } else {
                    List<String> sort = new ArrayList<>();
                    new ArrayList<>(Arrays.stream(Material.values()).map(Enum::name).toList())
                            .stream().filter(m -> m.contains(ss.toUpperCase()))
                            .forEach(sort::add);
                    return sort;
                }
            }
        }
        if (strings.length == 6) {
            String write = strings[0];
            if (write.equalsIgnoreCase("define")) {
                String ss = strings[5];
                if (ss.isBlank() || ss.isEmpty()) {
                    return Arrays.stream(Material.values()).map(Enum::toString).toList();
                } else {
                    List<String> sort = new ArrayList<>();
                    new ArrayList<>(Arrays.stream(Material.values()).map(Enum::name).toList())
                            .stream().filter(m -> m.contains(ss.toUpperCase()))
                            .forEach(sort::add);
                    return sort;
                }
            }
        }
        if (strings.length == 7) {
            String write = strings[0];
            if (write.equalsIgnoreCase("define")) {
                String ss = strings[6];
                if (ss.isBlank() || ss.isEmpty()) {
                    return MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList();
                } else {
                    List<String> sort = new ArrayList<>();
                    new ArrayList<>(MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList())
                            .stream().filter(m -> m.toUpperCase().contains(ss.toUpperCase()))
                            .forEach(sort::add);
                    return sort;
                }
            }
        }
        return null;
    }
}
