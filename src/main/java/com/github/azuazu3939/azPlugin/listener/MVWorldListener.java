package com.github.azuazu3939.azPlugin.listener;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.event.MVWorldDeleteEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MVWorldListener implements Listener {

    private static final Set<String> RESET_WORLD_NAMES = new HashSet<>();

    private final AzPlugin plugin;

    public MVWorldListener(AzPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isResetWorld(String worldName) {
        return RESET_WORLD_NAMES.contains(worldName);
    }

    @EventHandler
    public void onWorldDelete(@NotNull MVWorldDeleteEvent event) {
        MultiverseWorld world = event.getWorld();
        String worldName = world.getName();

        if (!isResourceWorld(worldName)) return;
        RESET_WORLD_NAMES.add(worldName);

        World bukkitWorld = world.getCBWorld();
        RegionManager regionManager = getRegionManager(bukkitWorld);
        if (regionManager == null) return;

        ProtectedRegion globalRegion = regionManager.getRegion(ProtectedRegion.GLOBAL_REGION);
        ProtectedRegion spawnRegion = regionManager.getRegion("spawn");
        if (globalRegion == null || spawnRegion == null) return;

        regenerateAndApplyFlags(worldName, world, globalRegion, spawnRegion);
    }

    private boolean isResourceWorld(@NotNull String worldName) {
        return worldName.toLowerCase().contains("open");
    }


    private RegionManager getRegionManager(World bukkitWorld) {
        com.sk89q.worldedit.world.World editWorld = BukkitAdapter.adapt(bukkitWorld);
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(editWorld);
    }

    private void regenerateAndApplyFlags(String worldName, MultiverseWorld world, ProtectedRegion globalRegion, ProtectedRegion spawnRegion) {
        plugin.runLater(() -> regenerateWorld(worldName, world), 50);
        plugin.runLater(() -> applyWorldGuardFlags(world.getCBWorld(), globalRegion.getFlags(), spawnRegion.getFlags()), 200);
    }

    private void regenerateWorld(String worldName, @NotNull MultiverseWorld world) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        WorldType worldType = world.getWorldType();
        String generator = world.getGenerator();
        World.Environment environment = world.getEnvironment();
        Difficulty difficulty = world.getDifficulty();

        if (core.getMVWorldManager().addWorld(worldName, environment, UUID.randomUUID().toString(), worldType, true, generator)) {
            initializeGeneratedWorld(worldName, difficulty);
        } else {
            handleWorldCreationFailure(worldName);
        }
    }

    private void initializeGeneratedWorld(String worldName, Difficulty difficulty) {
        plugin.runLater(() -> {
            World world = JavaPlugin.getPlugin(MultiverseCore.class).getMVWorldManager().getMVWorld(worldName).getCBWorld();
            if (world == null) return;
            configureWorldSettings(world, difficulty);
            plugin.getLogger().info(worldName + "の生成に成功しました。");
            if (MythicListener.isMythic()) {
                MythicListener.reloadMythic(100);
            }
            loadChunks(world);
            RESET_WORLD_NAMES.remove(worldName);
        }, 20);
    }

    private void handleWorldCreationFailure(String worldName) {
        plugin.getLogger().info(worldName + "の生成に失敗しました。");
        RESET_WORLD_NAMES.remove(worldName);
    }

    private void loadChunks(@NotNull World world) {
        plugin.runLater(() -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 50");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start " + world.getName());
            Bukkit.broadcast(Component.text("§b§l事前チャンクロードを準備中です…"));
            plugin.runLater(() -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "fcp start 2000 " + world.getName() + " 0 0");
                Bukkit.broadcast(Component.text("§a§l事前チャンクロードを開始しました！"));
            }, 100);
        }, 200);
    }

    private void applyWorldGuardFlags(World world, Map<Flag<?>, Object> globalFlags, Map<Flag<?>, Object> spawnFlags) {
        try {
            if (globalFlags == null || globalFlags.isEmpty()) return;

            com.sk89q.worldedit.world.World editWorld = BukkitAdapter.adapt(world);
            RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(editWorld);
            if (regionManager == null) return;

            ProtectedRegion globalRegion = regionManager.getRegion(ProtectedRegion.GLOBAL_REGION);
            if (globalRegion == null) return;

            ProtectedRegion spawnRegion = regionManager.getRegion("spawn");
            if (spawnRegion == null) return;

            globalRegion.setFlags(globalFlags);
            spawnRegion.setFlags(spawnFlags);
            List<ProtectedRegion> regions = Arrays.asList(globalRegion, spawnRegion);
            regionManager.setRegions(regions);
            regionManager.save();
        } catch (StorageException ignored) {
        }
    }

    public static void configureWorldSettings(@NotNull World world, Difficulty difficulty) {
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.KEEP_INVENTORY, true);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);
        world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setPVP(false);
        world.setDifficulty(difficulty);
        world.setSpawnLocation(0, 64, 0);
        world.getWorldBorder().setCenter(0, 0);

        if (!world.getName().contains("resource")) {
            configureWorldNotResourceSettings(world);
        } else {
            configureWorldResourceSettings(world);
        }
    }

    private static void configureWorldNotResourceSettings(@NotNull World world) {
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setViewDistance(6);
        world.setSimulationDistance(4);
    }

    private static void configureWorldResourceSettings(@NotNull World world) {
        world.getWorldBorder().setSize(50);
        world.getWorldBorder().setSize(10000, 3600);
        world.setGameRule(GameRule.MAX_ENTITY_CRAMMING, 7);
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 1);
        world.setViewDistance(8);
        world.setSimulationDistance(4);
    }
}
