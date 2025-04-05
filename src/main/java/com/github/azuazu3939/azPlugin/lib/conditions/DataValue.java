package com.github.azuazu3939.azPlugin.lib.conditions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DataValue {

    private final Player player;

    private static final Multimap<UUID, String> C_STRING = HashMultimap.create();
    private static final Multimap<UUID, Integer> C_COUNT = HashMultimap.create();

    private String TAG = "";
    private int COUNT = 0;
    private boolean value = false;

    public DataValue(Player player, boolean value) {
        this.player = player;
        this.value = value;
    }

    public DataValue(Player player, String TAG) {
        this.player = player;
        this.TAG = TAG;
    }

    public DataValue(Player player, int COUNT) {
        this.player = player;
        this.COUNT = COUNT;
    }

    public DataValue(Player player, String TAG, int COUNT) {
        this.player = player;
        this.TAG = TAG;
        this.COUNT = COUNT;
    }

    public boolean check() {
        boolean s = !TAG.isBlank() && !TAG.isEmpty();
        boolean c = COUNT != 0;

        if (s && c) {
            return C_COUNT.containsEntry(player.getUniqueId(), COUNT) && C_STRING.containsEntry(player.getUniqueId(), TAG);
        } else if (s) {
            return C_STRING.containsEntry(player.getUniqueId(), TAG);
        } else if (c) {
            return C_COUNT.containsEntry(player.getUniqueId(), COUNT);
        } else {
            return value;
        }
    }

    public String getTag() {
        return TAG;
    }

    public int getCount() {
        return COUNT;
    }

    public boolean isValue() {return value;}

    public DataValue add(int count) {
        this.COUNT += count;
        return this;
    }

    public DataValue add(String tag) {
        this.TAG += tag;
        return this;
    }

    public DataValue set(String tag) {
        this.TAG = tag;
        return this;
    }

    public DataValue set(int count) {
        this.COUNT = count;
        return this;
    }
}
