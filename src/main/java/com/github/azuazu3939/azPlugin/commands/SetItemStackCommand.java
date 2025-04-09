package com.github.azuazu3939.azPlugin.commands;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.DBBlockBreak;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.util.SetCommandUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
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

import java.util.*;

public class SetItemStackCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length < 2) {
            player.sendMessage(Component.text("///setDrop [<Material>)] [<mmid>] <amount(1to64>> <chance(0to1)> <reMineTick(default 200)> <ct_material(CTのマテリアルデフォ岩盤>"));
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

        String mmid = strings[1];
        int amount = 1;
        if (strings.length >= 3) {
            try {
                amount = Integer.parseInt(strings[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        double chance = 1;
        if (strings.length >= 4) {
            try {
                chance = Double.parseDouble(strings[3]);
            } catch (NumberFormatException ignored) {
            }
        }

        int tick = 200;
        if (strings.length >= 5) {
            try {
                tick = Integer.parseInt(strings[4]);
            } catch (NumberFormatException ignored) {
            }
        }

        Material ct_material = Material.BEDROCK;
        if (strings.length >= 6) {
            try {
                Material mm = Material.valueOf(strings[5].toUpperCase());
                if (mm != null && mm.isBlock()) {
                    ct_material = mm;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }

        int finalTick = tick;
        int finalAmount = amount;
        double finalChance = chance;
        Material finalCt_material = ct_material;
        AzPlugin.getInstance().runAsync(()-> {
            if (player == null) return;
            Set<Location> locations = SetCommandUtil.getLocations(player, box, material);

            int i = 2;
            for (Location loc : locations) {
                AzPlugin.getInstance().runAsyncLater(()-> DBBlockBreak.updateLocationAsync(DBCon.AbstractLocationSet.create(loc), finalTick, mmid, finalAmount, finalChance, finalCt_material), i);
                i += 2;
            }
            AzPlugin.getInstance().runAsyncLater(()-> player.sendMessage(Component.text("データの書き込みが終了しました。")), i);
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 1) {
            String arg = strings[0];
            if (arg.isBlank() || arg.isEmpty()) {
                return Arrays.stream(Material.values()).map(Enum::toString).toList();
            } else {
                List<String> list = new ArrayList<>();
                for (String mat : new ArrayList<>(Arrays.stream(Material.values()).map(Enum::toString).toList())) {
                    if (mat.toUpperCase().contains(arg.toUpperCase())) {
                        list.add(mat);
                    }
                }
                return list;
            }
        } else if (strings.length == 2) {
            String arg = strings[1];
            if (arg.isBlank() || arg.isEmpty()) {
                return MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList();
            } else {
                List<String> list = new ArrayList<>();
                for (String mat : new ArrayList<>(MythicBukkit.inst().getItemManager().getItems().stream().map(MythicItem::getInternalName).toList())) {
                    if (mat.toLowerCase().contains(arg.toLowerCase())) {
                        list.add(mat);
                    }
                }
                return list;
            }
        } else {
            return null;
        }
    }
}
