package com.github.azuazu3939.azPlugin.mana;

import com.github.azuazu3939.azPlugin.AzPlugin;
import com.github.azuazu3939.azPlugin.event.ManaModifiedEvent;
import com.github.azuazu3939.azPlugin.event.ManaModifyEvent;
import com.github.azuazu3939.azPlugin.util.ManaMultiplier;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.text.NumberFormat;

public class Mana {

    private final Player player;

    public static final NamespacedKey MANA_VALUE = new NamespacedKey("az", "mana");

    public static final NamespacedKey MAX_MANA = new NamespacedKey("az", "max_mana");

    protected final NumberFormat format = NumberFormat.getInstance();

    public Mana(Player player) {
        this.player = player;
    }

    public Player getPlayer() {return player;}

    public double getMana() {
       return Utils.getPlayerDataContainerDouble(player, MANA_VALUE, PersistentDataType.STRING, 0.0);
    }

    public double getMaxMana() {
        return getPlayerMaxMana() + getItemMaxMana();
    }

    protected double getPlayerMaxMana() {
        return Utils.getPlayerDataContainerDouble(player, MAX_MANA, PersistentDataType.STRING, 500.0);
    }

    protected double getItemMaxMana() {
        return Utils.getDataContainerDouble(player, MAX_MANA, PersistentDataType.STRING, Utils.getAllSlots());
    }

    public void setMana(double value) {
        AzPlugin.getInstance().run(()-> {
            double multi = ManaMultiplier.Adapter.getDouble(player.getUniqueId(), value);
            double lim = getManaLim(multi);
            ManaModifyEvent event = new ManaModifyEvent(player, getMana(), lim, getMaxMana());

            if (event.isCancelled()) return;
            player.getPersistentDataContainer().set(MANA_VALUE, PersistentDataType.STRING, String.valueOf(lim));
            new ManaModifiedEvent(player, getMana()).callEvent();
        });
    }

    protected double getManaLim(double value) {
        return Math.max(0, Math.min(getMaxMana(), value));
    }

    public void setMaxMana(double value) {
        player.getPersistentDataContainer().set(MAX_MANA, PersistentDataType.STRING, String.valueOf(value));
    }

    public boolean isFull() {
        return getMana() >= getMaxMana();
    }
}
