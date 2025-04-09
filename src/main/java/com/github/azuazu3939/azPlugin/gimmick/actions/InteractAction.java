package com.github.azuazu3939.azPlugin.gimmick.actions;

import com.github.azuazu3939.azPlugin.database.DBBlockInteract;
import com.github.azuazu3939.azPlugin.database.DBBlockInventory;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.gimmick.ShowCaseBuilder;
import com.github.azuazu3939.azPlugin.gimmick.holder.BaseAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.holder.EmptyAzHolder;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockInteractAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InteractAction extends BlockAction {

    public static void interactProcess(Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<String> op = DBBlockInteract.getLocationAction(set);
        if (op.isEmpty()) return;
        Optional<BlockInteractAction> op2 = DBBlockInventory.getLocationAction(op.get(), new EmptyAzHolder(6, "テスト").getInventory());
        if (op2.isEmpty()) return;
        BlockInteractAction action = op2.get();
        ShowCaseBuilder.create(player, new BaseAzHolder(6, "§b§lショップ§f: " + op.get(), action.inv(), action.cursor()));
    }
}
