package com.github.azuazu3939.azPlugin.lib.packet;

import org.bukkit.Material;

public record BlockBreakAction(int tick, String mmid, int amount, double chance, Material ct_material) {
}
