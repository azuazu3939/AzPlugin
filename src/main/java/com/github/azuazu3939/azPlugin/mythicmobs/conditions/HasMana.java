package com.github.azuazu3939.azPlugin.mythicmobs.conditions;

import com.github.azuazu3939.azPlugin.mana.Mana;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.api.skills.conditions.ISkillCondition;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.utils.numbers.RangedDouble;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HasMana implements ISkillCondition, IEntityCondition {

    private final RangedDouble amount;

    public HasMana(@NotNull MythicLineConfig config) {
        this.amount = new RangedDouble(config.getPlaceholderString(new String[]{"a", "amount"}, "10").get());
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        if (abstractEntity == null) return false;
        if (!abstractEntity.isPlayer()) return false;
        AbstractPlayer player = abstractEntity.asPlayer();
        Player p = BukkitAdapter.adapt(player);
        return amount.equals(new Mana(p).getMana());
    }
}
