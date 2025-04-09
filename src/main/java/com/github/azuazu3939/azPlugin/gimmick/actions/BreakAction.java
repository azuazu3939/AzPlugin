package com.github.azuazu3939.azPlugin.gimmick.actions;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.database.DBBlockBreak;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.gimmick.records.BlockBreakAction;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;

public class BreakAction extends BlockAction {

    public static void breakProcess(Player player, @NotNull DBCon.AbstractLocationSet set) {
        Optional<BlockBreakAction> op = DBBlockBreak.getLocationAction(set);
        if (op.isPresent()) {
            BlockBreakAction action = op.get();
            long tick;
            String mmid = action.mmid();
            Random ran = new Random();

            BlockPos ps = new BlockPos(set.x(), set.y(), set.z());
            if (isAffected(player.getUniqueId(), ps)) {
                cooldown(player, ps, action);
                return;
            }

            if (mmid != null && ran.nextDouble() < action.chance()) {
                dropItemStack(player, mmid, action);
            }

            tick = action.tick();
            player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1 ,1);
            mined(player, ps, action, tick);
        }
    }

    protected static void mined(Player player,  BlockPos ps, BlockBreakAction action, long tick) {
        AzPlugin.getInstance().runAsyncLater(() -> {
            PacketHandler.changeBlock(player, ps, action.ct_material());
            if (tick > 0) {
                put(player.getUniqueId(), ps, tick);
            }
        }, 1);
    }
}
