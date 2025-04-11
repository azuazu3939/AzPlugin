package com.github.azuazu3939.azPlugin.gimmick.records;

import org.bukkit.Material;

public record BlockPlaceAction(int tick, Material material, String trigger, String mmid) implements BlockAction {
}
