package com.github.azuazu3939.azPlugin.gimmick.actions;

import com.github.azuazu3939.azPlugin.database.DBBlockPlace;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockPlaceAction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlaceAction extends BlockAction {

    public static void placeProcess(Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockPlaceAction> op = DBBlockPlace.getLocationAction(set);
    }
}
