package com.github.azuazu3939.azPlugin.mythicmobs.mechanics;

import com.github.azuazu3939.azPlugin.AzPlugin;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class RaidBoss implements ISkillMechanic, ITargetedEntitySkill {

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, @NotNull AbstractEntity abstractEntity) {
        if (abstractEntity.isPlayer()) return SkillResult.CONDITION_FAILED;
        if (!abstractEntity.isLiving()) return SkillResult.CONDITION_FAILED;

        NamespacedKey key = new NamespacedKey(AzPlugin.getInstance(), "raid_boss");
        abstractEntity.getDataContainer().set(key, PersistentDataType.STRING, "true");
        return SkillResult.SUCCESS;
    }
}