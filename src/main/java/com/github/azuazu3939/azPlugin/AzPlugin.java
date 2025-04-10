package com.github.azuazu3939.azPlugin;

import com.github.azuazu3939.azPlugin.commands.*;
import com.github.azuazu3939.azPlugin.database.*;
import com.github.azuazu3939.azPlugin.packet.PacketHandler;
import com.github.azuazu3939.azPlugin.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AzPlugin extends JavaPlugin {

    private static AzPlugin instance;

    public static AzPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        registerListeners();
        registerCommands();
        new Azurite(this);
    }

    @Override
    public void onDisable() {
        ManaListener.removeAll();
        DBCon.close();
        Bukkit.getOnlinePlayers().forEach(PacketHandler::eject);
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ManaListener(this), this);
        pm.registerEvents(new MythicListener(), this);
        pm.registerEvents(new MythicDamageListener(), this);
        pm.registerEvents(new UniqueSkillListener(), this);
        pm.registerEvents(new MythicDisplayListener(), this);
        pm.registerEvents(new MVWorldListener(this), this);
        pm.registerEvents(new EntityDamageListener(), this);
        pm.registerEvents(new EatListener(this), this);
        pm.registerEvents(new PlayerAttackListener(), this);
        pm.registerEvents(new PlayerCommandListener(), this);
        pm.registerEvents(new DamageCalculationListener(), this);
        pm.registerEvents(new PacketBlockListener(), this);
        pm.registerEvents(new GlobalSettingsListener(), this);
        pm.registerEvents(new GoldenAxeListener(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("worldregen")).setExecutor(new WorldRegenCommand(this));
        Objects.requireNonNull(getCommand("worldset")).setExecutor(new WorldSetCommand());
        Objects.requireNonNull(getCommand("worldcreate")).setExecutor(new WorldCreateCommand());
        Objects.requireNonNull(getCommand("worldteleport")).setExecutor(new WorldTeleportCommand());
        Objects.requireNonNull(getCommand("mode")).setExecutor(new ModeCommand());
        Objects.requireNonNull(getCommand("setmana")).setExecutor(new SetManaCommand());
        Objects.requireNonNull(getCommand("setmaxmana")).setExecutor(new SetMaxManaCommand());
        Objects.requireNonNull(getCommand("dungeon")).setExecutor(new DungeonCommand());
        Objects.requireNonNull(getCommand("viewer")).setExecutor(new AttributeViewer());
        Objects.requireNonNull(getCommand("//pos1")).setExecutor(new PositionCommand());
        Objects.requireNonNull(getCommand("//pos2")).setExecutor(new PositionCommand());
        Objects.requireNonNull(getCommand("//temp")).setExecutor(new TempCommand());
    }

    @NotNull
    public BukkitTask run(Runnable runnable) {
        return Bukkit.getScheduler().runTask(this, runnable);
    }

    @NotNull
    public BukkitTask runLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(this, runnable, delay);
    }

    @NotNull
    public BukkitTask runTimer(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(this, runnable, delay, period);
    }

    @NotNull
    public BukkitTask runAsync(Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(this, runnable);
    }

    @NotNull
    public BukkitTask runAsyncLater(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(this, runnable, delay);
    }

    @NotNull
    public BukkitTask runAsyncTimer(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, period);
    }
}
