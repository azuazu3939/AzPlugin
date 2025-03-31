package com.github.azuazu3939.azPlugin.mythicmobs.mechanics;

import com.github.azuazu3939.azPlugin.mana.Mana;
import com.github.azuazu3939.azPlugin.mana.ManaRegen;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ModifyMana implements ISkillMechanic, ITargetedEntitySkill {

    private long add;

    private double multiply;

    private final boolean multiple;

    public ModifyMana(@NotNull MythicLineConfig config) {
        try {
            this.add = Long.parseLong(config.getPlaceholderString(new String[]{"a", "amount"}, "1").get());
        } catch (NumberFormatException e) {
            this.add = 1;
        }
        try {
            this.multiply = Double.parseDouble(config.getPlaceholderString(new String[]{"a", "amount"}, "0.01").get());
        } catch (NumberFormatException e) {
            this.multiply = 0.01;
        }
        this.multiple = config.getBoolean(new String[]{"m", "multiple"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (abstractEntity == null) return SkillResult.INVALID_TARGET;
        if (!abstractEntity.isPlayer()) return SkillResult.INVALID_TARGET;
        Player player = BukkitAdapter.adapt(abstractEntity.asPlayer());
        if (multiple) {
            new ManaRegen(player).forceRegen(multiply);
        } else {
            Mana mana = new Mana(player);
            mana.setMana(Math.min(add + mana.getMana(), mana.getMaxMana()));
        }
        return SkillResult.SUCCESS;
    }
}
