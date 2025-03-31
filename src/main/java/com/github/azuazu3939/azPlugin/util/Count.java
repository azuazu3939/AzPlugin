package com.github.azuazu3939.azPlugin.util;

import java.util.UUID;

public abstract class Count implements ICount {

    private final UUID uuid;

    protected Count(UUID uuid, double tick) {
        this.uuid = uuid;
    }

    public Count(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean build(UUID uuid, double tick) {
        return false;
    }

    public double getBase(UUID uuid) {
        return 1.0;
    }

    public UUID getUUID() {
        return uuid;
    }
}

