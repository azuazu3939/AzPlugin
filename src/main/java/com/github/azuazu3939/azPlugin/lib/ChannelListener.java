package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.listener.PacketBlockListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ChannelListener extends ChannelDuplexHandler {

    private final Player player;

    private final ServerPlayer sp;

    public ChannelListener(Player player) {
        this.player = player;
        this.sp = ((CraftPlayer) player).getHandle();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // server -> client
        if (msg instanceof ClientboundOpenScreenPacket packet) {
            ChestMenu cc = getCraftContainer(packet.getContainerId());
            Inventory inv = cc.getBukkitView().getTopInventory();
            if (ShowCaseBuilder.checkHolder(inv)) {
                PacketHandler.sendItemPacket(player, packet.getContainerId(), cc.getStateId(), 1);
            }
        }

        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // client -> server
        if (msg instanceof ServerboundUseItemOnPacket packet) {
            BlockHitResult result = packet.getHitResult();
            if (result != null && PacketBlockListener.isAffected(player.getUniqueId(), result.getBlockPos())) return;

        } else if (msg instanceof ServerboundContainerClickPacket packet) {

            InventoryView view = getCraftContainer(packet.getContainerId()).getBukkitView();
            Inventory top = view.getTopInventory();

            if (ShowCaseBuilder.checkHolder(top)) {
                for (int i : packet.getChangedSlots().keySet()) {

                    AzPlugin.getInstance().runAsyncLater(() -> {
                        PacketHandler.sendSetSlot(player, -1, packet.getStateId(), -1, ShowCaseBuilder.getEmpty());

                        ItemStack item = (i < top.getSize()) ? ShowCaseBuilder.get(player.getUniqueId()).items().get(i) : ItemStack.fromBukkitCopy(view.getItem(i));

                        if (PacketHandler.isNoAction(player.getUniqueId())) return;
                        PacketHandler.sendSetSlot(player, packet.getContainerId(), packet.getStateId(), i, item);
                    }, 1);
                }
                return;
            }
        } else if (msg instanceof ServerboundContainerClosePacket packet) {
            Inventory top = getCraftContainer(packet.getContainerId()).getBukkitView().getTopInventory();
            if (ShowCaseBuilder.checkHolder(top)) {
                ShowCaseBuilder.remove(player.getUniqueId());
            }
        }
        super.channelRead(ctx, msg);
    }

    @NotNull
    @Contract("_ -> new")
    private ChestMenu getCraftContainer(int containerId) {
        int row = player.getOpenInventory().getTopInventory().getSize() / 9;
        if (row == 1) {
            return ChestMenu.oneRow(containerId, sp.getInventory());
        } else if (row == 2) {
            return ChestMenu.twoRows(containerId, sp.getInventory());
        } else if (row == 3) {
            return ChestMenu.threeRows(containerId, sp.getInventory());
        } else if (row == 4) {
            return ChestMenu.fourRows(containerId, sp.getInventory());
        } else if (row == 5) {
            return ChestMenu.fiveRows(containerId, sp.getInventory());
        } else {
            return ChestMenu.sixRows(containerId, sp.getInventory());
        }
    }
}
