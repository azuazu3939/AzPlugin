package com.github.azuazu3939.azPlugin;

import com.github.azuazu3939.azPlugin.commands.*;
import com.github.azuazu3939.azPlugin.database.DBCon;
import com.github.azuazu3939.azPlugin.listener.*;
import com.github.azuazu3939.azPlugin.mana.Mana;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import com.github.azuazu3939.azPlugin.unique.Skill;
import com.github.azuazu3939.azPlugin.unique.armor.BlessingOfTheEarth;
import com.onarandombox.MultiverseCore.MultiverseCore;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.azisaba.loreeditor.api.event.EventBus;
import net.azisaba.loreeditor.api.event.ItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

        registerDB();
        registerListeners();
        registerCommands();

        MythicListener.reloadMythic(20);
        onlinePlayer();

        registerLore();
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

    private void registerLore() {
        EventBus.INSTANCE.register(this, ItemEvent.class, 0, e -> {
            uniqueRegister(e);
            manaRegister(e);
            weaponRegister(e);
        });
    }

    private void onlinePlayer() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            new ManaRegen(player).start();
            BlessingOfTheEarth.System.addMember(player);
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

    private void manaRegister(@NotNull ItemEvent e) {
        double mana = getItemMana(e.getBukkitItem());
        if (mana == -1) return;

        String manaMessage = "§bマナ +V".replace("V", String.valueOf(mana));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§7装備したとき："));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(manaMessage));
    }

    private void uniqueRegister(@NotNull ItemEvent e) {
        ItemStack item = e.getBukkitItem();
        Player p = e.getPlayer();
        Skill.getSkills(item).forEach(s -> {
            int i = Skill.getItemLevel(item, s.getKey());
            if (i == -1) return;
            e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(s.getName() + " " + s.getString(i, p)));
            s.getLore().forEach(l -> e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(l)));
        });
    }

    private double getItemMana(@NotNull ItemStack item) {
        if (!item.hasItemMeta()) return -1;
        PersistentDataContainer pc = item.getItemMeta().getPersistentDataContainer();
        String s = pc.get(Mana.MAX_MANA, PersistentDataType.STRING);
        if (s == null) return -1;
        try {
            return Math.ceil(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void weaponRegister(@NotNull ItemEvent e) {
        String mmid = MythicBukkit.inst().getItemManager().getMythicTypeFromItem(e.getBukkitItem());
        if (mmid == null) return;
        MythicItem mi = MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        if (mi == null) return;
        String group = mi.getGroup();
        if (group == null) return;
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text(""));
        e.addLore(net.azisaba.loreeditor.libs.net.kyori.adventure.text.Component.text("§fカテゴリー: §7" + group));
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
