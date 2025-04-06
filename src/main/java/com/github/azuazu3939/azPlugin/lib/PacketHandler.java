package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitBlock;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PacketHandler {

    private static final String NAME = "az";

    private static void sendPacket(Player p, net.minecraft.network.protocol.Packet<?> packet) {
        ((CraftPlayer) p).getHandle().connection.sendPacket(packet);
    }

    private static Channel getChannel(Player player) {
        return ((CraftPlayer) player).getHandle().connection.connection.channel;
    }

    public static void spawnTextDisplay(@NotNull Player p, double x, double y, double z, int id) {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(id, UUID.randomUUID(), x, y, z, 0f, 0f,
                EntityType.TEXT_DISPLAY, id, Vec3.ZERO, 0);
        sendPacket(p, packet);

    }

    public static void setTextDisplayMeta(Player p, int id, Component text) {
        net.minecraft.network.chat.Component comp = PaperAdventure.asVanilla(text);
        List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.COMPONENT.createAccessor(23), comp));
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(15), (byte) 3));
        list.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BYTE.createAccessor(26), (byte) 255));
        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(id, list);

        sendPacket(p, packet);
    }

    public static void removePacketEntity(Player p, int id) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(id);
        sendPacket(p, packet);
        p.getWorld().spawn(p.getLocation(), BlockDisplay.class);
    }

    public static void changeBlock(Player p, @NotNull BlockPos pos, Material material) {
        MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendBlockChange(

                Collections.singleton(BukkitAdapter.adapt(p)),
                BukkitAdapter.adapt(new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ())),
                new BukkitBlock(material));
    }

    public static void undoEffected(Player p, BlockPos pos) {
        if (p == null) return;
        ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(((CraftWorld) p.getWorld()).getHandle(), pos);
        sendPacket(p, packet);
    }

    public static void inject(Player player) {
        try {
            getChannel(player).pipeline().addBefore("packet_handler", NAME, new ChannelListener(player));
            AzPlugin.getInstance().getLogger().info("Injected packet handler for " + player.getName());
        } catch (Exception e) {
            AzPlugin.getInstance().runLater(()-> {
                if (!player.isOnline()) return;
                try {
                    getChannel(player).pipeline().addBefore("packet_handler", NAME, new ChannelListener(player));
                    AzPlugin.getInstance().getLogger().info("Injected packet handler for " + player.getName());
                } catch (Exception ex) {
                    AzPlugin.getInstance().getLogger().warning("Failed to inject packet handler to " + player.getName());
                }
            }, 10);
        }
    }

    public static void eject(Player player) {
        try {
            ChannelPipeline pipeline = getChannel(player).pipeline();
            if (pipeline != null) {
                pipeline.remove(NAME);
                AzPlugin.getInstance().getLogger().info("Ejected packet handler from " + player.getName());
            }
        } catch (Exception e) {
            AzPlugin.getInstance().getLogger().info("Failed to eject packet handler from " + player.getName() + ", are they already disconnected?");
        }
    }
}
