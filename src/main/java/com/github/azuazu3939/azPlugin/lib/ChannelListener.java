package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.listener.PacketBlockListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChannelListener extends ChannelDuplexHandler {

    private final Player player;

    public ChannelListener(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ServerboundUseItemOnPacket packet) {
            if (PacketBlockListener.isAffected(player.getUniqueId(), packet.getHitResult().getBlockPos())) return;

        } else if (msg instanceof ClientboundContainerSetContentPacket packet) {
            if (checkHolder(ctx, msg, packet.getContainerId())) return;
            sendFixPacket(ctx, packet.getContainerId(), packet.getStateId());
            return;

        } else if (msg instanceof ServerboundContainerClickPacket packet) {
            if (checkHolder(ctx, msg, packet.getContainerId())) return;
            sendFixPacket(ctx, packet.getContainerId(), packet.getStateId());
            return;
        }
        super.channelRead(ctx, msg);
    }

    private boolean checkHolder(ChannelHandlerContext ctx, Object msg, int containerId) throws Exception {
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        CraftContainer cc = new CraftContainer(player.getOpenInventory(), sp, containerId);
        Inventory inv = cc.getBukkitView().getTopInventory();

        if (!(inv.getHolder() instanceof AzHolder)) {
            super.channelRead(ctx, msg);
            return true;
        }
        return false;
    }

    private void sendFixPacket(ChannelHandlerContext ctx, int container, int state) throws Exception {
        ClientboundContainerSetContentPacket send = new ClientboundContainerSetContentPacket(
                container,
                state,
                getItemStacks(),
                getEmpty());
        super.channelRead(ctx, send);
    }


    @NotNull
    private static ItemStack getEmpty() {
        return ItemStack.fromBukkitCopy( new org.bukkit.inventory.ItemStack(Material.AIR));
    }

    @NotNull
    private static NonNullList<ItemStack>  getItemStacks() {
        ItemStack B = ItemStack.fromBukkitCopy(new org.bukkit.inventory.ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        ItemStack V = ItemStack.fromBukkitCopy(new org.bukkit.inventory.ItemStack(Material.DIAMOND_PICKAXE));
        ItemStack N = getEmpty();

        NonNullList<ItemStack> items = NonNullList.create();
        items.addAll(Arrays.stream(new ItemStack[]{
                B, B, B, B, B, B, B, B, B,
                B, N, N, N, N, N, N, N, B,
                B, N, V, V, V, V, V, N, B,
                B, N, V, V, V, V, V, N, B,
                B, N, N, N, N, N, N, N, B,
                B, B, B, B, B, B, B, B, B
        }).toList());

        return items;
     }
}
