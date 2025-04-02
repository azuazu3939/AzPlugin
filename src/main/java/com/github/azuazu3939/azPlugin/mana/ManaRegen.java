package com.github.azuazu3939.azPlugin.mana;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ManaRegen extends Mana {

    private final Player player;

    private static final Map<UUID, BukkitTask> TASKS = new HashMap<>();

    public static final NamespacedKey MANA_REGEN = new NamespacedKey("az", "mana_regen");

    private double regen = 10.0;

    public double getBaseRegen() {return regen;}

    public double getFinalRegen() {
        return getBaseRegen()
                + Utils.getDataContainerDouble(player, MANA_REGEN, PersistentDataType.STRING, Utils.getAllSlots())
                + Utils.getPlayerDataContainerDouble(player, MANA_REGEN, PersistentDataType.STRING, 0.0);
    }

    public ManaRegen(Player player) {
        super(player);
        this.player = player;
    }

    public void start() {
        BukkitTask task = AzPlugin.getInstance().runAsyncTimer(()->
                setMana(Math.min(getMaxMana(), getFinalRegen() + getMana())), 10, 10);
        TASKS.put(player.getUniqueId(), task);
    }

    public void stop() {
        if (TASKS.containsKey(player.getUniqueId())) {
            TASKS.get(player.getUniqueId()).cancel();
        }
        TASKS.remove(player.getUniqueId());
    }

    public void setBaseRegen(double regen) {
        this.regen = regen;
    }

    public void forceRegen(double percentage) {
        setMana(Math.min(getMaxMana(), getMana() + getMaxMana() * percentage));
    }
}
