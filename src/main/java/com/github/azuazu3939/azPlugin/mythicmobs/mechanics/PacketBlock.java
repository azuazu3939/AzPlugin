package com.github.azuazu3939.azPlugin.mythicmobs.mechanics;

import com.github.azuazu3939.azPlugin.database.DBLocation;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketBlock implements ISkillMechanic, ITargetedLocationSkill {

    private final int tick;
    private final Material material;

    public PacketBlock(@NotNull MythicLineConfig config) {
        tick = PlaceholderInt.of(config.getString(new String[]{"tick", "t"}, "200")).get();
        Material m = Material.valueOf(PlaceholderString.of(config.getString(new String[]{"mmid, mm", "stone"})).get().toUpperCase());
        material = m == null ? Material.STONE : m;
    }

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        if (!(skillMetadata.getCaster().getEntity().getBukkitEntity() instanceof Player)) return SkillResult.CONDITION_FAILED;

        Location loc = BukkitAdapter.adapt(abstractLocation);
        DBLocation.updateLocationAsync(loc, tick, null, 0, material, 0);
        return SkillResult.SUCCESS;
    }
}
