package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.listener.PacketBlockListener;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.game.*;
import org.bukkit.entity.Player;

public class ChannelListener extends ChannelDuplexHandler {

    private final Player player;

    public ChannelListener(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ServerboundUseItemOnPacket packet) {
            if (PacketBlockListener.isAffected(player.getUniqueId(), packet.getHitResult().getBlockPos())) return;
        }
        super.channelRead(ctx, msg);
    }
}
