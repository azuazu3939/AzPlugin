package com.github.azuazu3939.azPlugin;

import com.github.azuazu3939.azPlugin.commands.*;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.lib.Lore;
import com.github.azuazu3939.azPlugin.listener.*;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.unique.armor.BlessingOfTheEarth;
import com.github.azuazu3939.azPlugin.unique.armor.Defence;
import com.onarandombox.MultiverseCore.MultiverseCore;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class AzPlugin extends JavaPlugin {

    private static AzPlugin instance;
    private static Lore LORE;

    public static AzPlugin getInstance() {
        return instance;
    }
    public static Lore getLore() {return LORE;}

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        registerDB();
        registerListeners();
        registerCommands();

        MythicListener.reloadMythic(20);
        onlinePlayer();

        LORE = new Lore(this);
        LORE.register();
    }

    @Override
    public void onDisable() {
        ManaListener.removeAll();
        DBCon.close();
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
        pm.registerEvents(new LootChestListener(this), this);
        pm.registerEvents(new ResourceWorldListener(), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new PlayerWorldChangeListener(), this);
        pm.registerEvents(new PlayerAttackListener(), this);
        pm.registerEvents(new PlayerCommandListener(), this);

        pm.registerEvents(new OpenFieldListener(), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("worldregen")).setExecutor(new WorldRegenCommand(this));
        Objects.requireNonNull(getCommand("worldset")).setExecutor(new WorldSetCommand());
        Objects.requireNonNull(getCommand("worldcreate")).setExecutor(new WorldCreateCommand(this));
        Objects.requireNonNull(getCommand("worldteleport")).setExecutor(new WorldTeleportCommand());
        Objects.requireNonNull(getCommand("mode")).setExecutor(new ModeCommand());
        Objects.requireNonNull(getCommand("setmana")).setExecutor(new SetManaCommand());
        Objects.requireNonNull(getCommand("setmaxmana")).setExecutor(new SetMaxManaCommand());
        Objects.requireNonNull(getCommand("dungeon")).setExecutor(new DungeonCommand());
    }

    private void registerDB() {
        runAsync(()-> new DBCon().initialize(this));
    }

    private void onlinePlayer() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            new ManaRegen(player).start();
            BlessingOfTheEarth.System.addMember(player);
            new Defence.System(player).apply();
        });
    }

    public void createWorld(String name, String gen, Difficulty dif, WorldType type, World.Environment environment, String seed, boolean generate) {
        MultiverseCore core = JavaPlugin.getPlugin(MultiverseCore.class);
        if (core.getMVWorldManager()
                .addWorld(name, environment, seed, type, generate, gen)) {

            runLater(()-> {
                World w = core.getMVWorldManager().getMVWorld(name).getCBWorld();
                if (w == null) return;
                MVWorldListener.configureWorldSettings(w, dif);

                if (MythicListener.isMythic()) {
                    MythicListener.reloadMythic(100);
                }
            }, 20);
        }
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
