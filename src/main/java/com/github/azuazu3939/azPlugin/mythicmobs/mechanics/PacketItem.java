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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PacketItem implements ISkillMechanic, ITargetedLocationSkill {

    private final int tick;
    private final String mmid;
    private final int amount;
    private final double chance;

    public PacketItem(@NotNull MythicLineConfig config) {
        tick = PlaceholderInt.of(config.getString(new String[]{"tick", "t"}, "200")).get();
        mmid = PlaceholderString.of(config.getString(new String[]{"mmid, mm", "stone"})).get();
        amount = PlaceholderInt.of(config.getString(new String[]{"amount", "a"}, "1")).get();
        chance = PlaceholderInt.of(config.getString(new String[]{"chance", "c"}, "1")).get();
    }

    @Override
    public SkillResult castAtLocation(@NotNull SkillMetadata skillMetadata, AbstractLocation abstractLocation) {
        if (!(skillMetadata.getCaster().getEntity().getBukkitEntity() instanceof Player p)) return SkillResult.CONDITION_FAILED;

        Location loc = BukkitAdapter.adapt(abstractLocation);
        DBLocation.updateLocationAsync(loc, tick, mmid, amount, null, chance);
        return SkillResult.SUCCESS;
    }
}
