package com.github.azuazu3939.azPlugin.packet;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.gimmick.ShowCaseBuilder;
import io.lumine.mythic.api.adapters.AbstractBlock;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitBlock;
import io.lumine.mythic.bukkit.adapters.BukkitPlayer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PacketHandler {

    private static final String NAME = "az";

    public static void sendPacket(Player p, net.minecraft.network.protocol.Packet<?> packet) {
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

    public static void multiChangeBlock(Player triggerPlayer, @NotNull Collection<Player> multiPlayer, @NotNull Collection<BlockPos> poss, Material material) {
        Map<AbstractLocation, AbstractBlock> sendBlocks = new HashMap<>();
        poss.forEach(pos ->
                sendBlocks.put(BukkitAdapter.adapt(
                        new Location(triggerPlayer.getWorld(), pos.getX(), pos.getY(), pos.getZ())),
                        new BukkitBlock(material)
                )
        );
        Set<AbstractPlayer> sendPlayers = new HashSet<>();
        multiPlayer.forEach(player -> {
            sendPlayers.add(BukkitAdapter.adapt(player));
        });
        MythicBukkit.inst().getVolatileCodeHandler().getBlockHandler().sendMultiBlockChange(
                sendPlayers,
                sendBlocks
        );
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

    public static void sendItemPacket(@NotNull Player player, int container, int state, long scale) {
        NonNullList<ItemStack> list = ShowCaseBuilder.get(player.getUniqueId()).items();
        long delay = list.size() * scale;
        if (!list.isEmpty()) {
            sendContainerItemPacket(player, container, state, scale, list);
        }
        long finish = delay + 6 * scale + 1;
        sendCursorItemPacket(player, state, finish);
    }

    private static void sendContainerItemPacket(@NotNull Player player, int container, int state, long scale, @NotNull NonNullList<ItemStack> list) {
        for (int i = 0; i < list.size(); i++) {
            ClientboundContainerSetSlotPacket set = new ClientboundContainerSetSlotPacket(container, state, i, list.get(i));
            int finalI = i;
            AzPlugin.getInstance().runAsyncLater(()-> {
                PacketHandler.sendPacket(player, set);
                if (finalI % 2 == 0) {
                    player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 0.2F,  (float) (0.75 + finalI * 0.01));
                }
            }, i * scale + 1);
        }
    }

    private static void sendCursorItemPacket(@NotNull Player player, int state, long finish) {
        ClientboundContainerSetSlotPacket cursor = new ClientboundContainerSetSlotPacket(-1, state, -1, ShowCaseBuilder.getEmpty());
        AzPlugin.getInstance().runAsyncLater(()->
                PacketHandler.sendPacket(player, cursor), finish);
        AzPlugin.getInstance().runAsyncLater(()-> player.playSound(player, Sound.ENTITY_CHICKEN_EGG, 1F,  0.5F), finish + 20);
    }

    public static void sendSetSlot(Player player, int containerId, int state, int slot, ItemStack item) {
        ClientboundContainerSetSlotPacket set = new ClientboundContainerSetSlotPacket(
                containerId,
                state,
                slot,
                item);
        PacketHandler.sendPacket(player, set);
    }
}
