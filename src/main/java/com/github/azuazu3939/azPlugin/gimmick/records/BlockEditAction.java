package com.github.azuazu3939.azPlugin.gimmick.records;

import com.github.azuazu3939.azPlugin.database.DBCon;
import org.bukkit.Material;

import java.util.Set;

public record BlockEditAction(Set<DBCon.AbstractLocationSet> set, int tick, Material material) implements BlockAction {
}
