package com.github.azuazu3939.azPlugin.gimmick.records;

import org.bukkit.Material;

public record BlockDropAction(String mmid, int amount, double chance, int tick, Material ct_material) implements BlockAction {
}
