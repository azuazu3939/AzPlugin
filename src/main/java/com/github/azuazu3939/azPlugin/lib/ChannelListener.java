package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.listener.PacketBlockListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
            CraftContainer cc = new CraftContainer(player.getOpenInventory(), sp, packet.getContainerId());
            Inventory inv = cc.getBukkitView().getTopInventory();
            if (checkHolder(inv)) {
                sendItemPacket(packet.getContainerId(), cc.incrementStateId(), inv.getSize());
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // client -> server
        if (msg instanceof ServerboundUseItemOnPacket packet) {
            if (PacketBlockListener.isAffected(player.getUniqueId(), packet.getHitResult().getBlockPos())) return;

        } else  if (msg instanceof ServerboundContainerClickPacket packet) {
            CraftContainer cc = new CraftContainer(player.getOpenInventory(), sp, packet.getContainerId());
            Inventory top = cc.getBukkitView().getTopInventory();
            if (checkHolder(top)) {
                for (int i : packet.getChangedSlots().keySet()) {

                    AzPlugin.getInstance().runAsyncLater(()-> {
                        ItemStack item = (i < top.getSize()) ? ShowCaseBuilder.get(player.getUniqueId()).items().get(i) : ItemStack.fromBukkitCopy(cc.getBukkitView().getItem(i));
                        sendSetSlot(packet.getContainerId(), packet.getStateId(), i, item);
                        sendSetSlot(-1, packet.getStateId(), -1, ShowCaseBuilder.getEmpty());
                    }, 1);
                }
                return;
            }
        } else if (msg instanceof ServerboundContainerClosePacket packet) {
            CraftContainer cc = new CraftContainer(player.getOpenInventory(), sp, packet.getContainerId());
            Inventory top = cc.getBukkitView().getTopInventory();
            if (checkHolder(top)) {
                ShowCaseBuilder.remove(player.getUniqueId());
            }
        }
        super.channelRead(ctx, msg);
    }

    private void sendSetSlot(int containerId, int state, int slot, ItemStack item) {
        ClientboundContainerSetSlotPacket set = new ClientboundContainerSetSlotPacket(
                containerId,
                state,
                slot,
                item);
        PacketHandler.sendPacket(player, set);
    }

    private boolean checkHolder(@NotNull Inventory inv) {
        return inv.getHolder() instanceof ShowCaseBuilder;
    }

    private void sendItemPacket(int container, int state, int size) {
        for (int i = 0; i < size; i++) {
            ClientboundContainerSetSlotPacket set = new ClientboundContainerSetSlotPacket(container, state, i, ShowCaseBuilder.get(player.getUniqueId()).items().get(i));
            int finalI = i;
            AzPlugin.getInstance().runAsyncLater(()-> {
                PacketHandler.sendPacket(player, set);
                if (finalI % 2 == 0) {
                    player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 0.2F,  (float) (0.75 + finalI * 0.01));
                }
            }, i);
        }
        ClientboundContainerSetSlotPacket cursor = new ClientboundContainerSetSlotPacket(-1, state, -1, ShowCaseBuilder.getEmpty());
        AzPlugin.getInstance().runAsyncLater(()->
                PacketHandler.sendPacket(player, cursor), 60);
        AzPlugin.getInstance().runAsyncLater(()->
                player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1F,  0.5F), 74);
    }
}
