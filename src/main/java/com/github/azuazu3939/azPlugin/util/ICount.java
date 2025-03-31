package com.github.azuazu3939.azPlugin.util;

import java.util.UUID;

public interface ICount {

    boolean build(UUID uuid, double tick);

    double getBase(UUID uuid);

    UUID getUUID();
}
