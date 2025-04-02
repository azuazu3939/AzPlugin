package com.github.azuazu3939.azPlugin.unique;

import com.github.azuazu3939.azPlugin.unique.armor.*;
import com.github.azuazu3939.azPlugin.util.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("StaticInitializerReferencesSubClass")
public abstract class Skill implements SkillBase {

    public static final Skill BLESSING_OF_THE_EARTH = new BlessingOfTheEarth();
    public static final Skill DIVINE_BLESSING = new DivineBlessing();
    public static final Skill HIT_AND_SPEED = new HitAndSpeed();
    public static final Skill SLOW_SPEED = new SlowLife();
    public static final Skill SPEED_HOLDER = new SpeedHolder();
    public static final Skill DEFENCE = new Defence();

    private static final List<Skill> SKILLS = new ArrayList<>();

    static {
        SKILLS.add(BLESSING_OF_THE_EARTH);
        SKILLS.add(DIVINE_BLESSING);
        SKILLS.add(HIT_AND_SPEED);
        SKILLS.add(SLOW_SPEED);
        SKILLS.add(SPEED_HOLDER);
        SKILLS.add(DEFENCE);
    }

    @Nullable
    public static Skill of(NamespacedKey key) {
        return SKILLS.stream().filter(skill -> skill.getKey().getKey().equals(key.getKey())).findFirst().orElse(null);
    }

    public static List<Skill> getSkills() {return SKILLS;}

    public static int getUniqueLevel(@NotNull NamespacedKey key, ItemStack item) {
        String data = Utils.getItemDataContainerString(item, key, PersistentDataType.STRING);
        return (data == null) ? -1 : Integer.parseInt(data);
    }

    @NotNull
    public static Set<Skill> getSkills(ItemStack item) {
        Set<Skill> set = new HashSet<>();
        if (item == null || !item.hasItemMeta()) return set;
        item.getItemMeta().getPersistentDataContainer().getKeys().forEach(n -> {

            Skill skill = Skill.of(n);
            if (skill == null) return;

            set.add(skill);

        });
        return set;
    }

    private final NamespacedKey key;
    private final int maxLevel;

    public Skill(NamespacedKey key, int maxLevel) {
        this.key = key;
        this.maxLevel = maxLevel;
    }

    @Override
    public int getLevel(Player player) {
        return Math.min(maxLevel, getAsInt(player));
    }

    @Override
    public String getString(int partsLevel, Player player) {
        return partsLevel + "/"+ getLevel(player) + "/" + maxLevel;
    }

    @Override
    public int getAsInt(Player player) {
        return Utils.getDataContainerInt(player, getKey(), PersistentDataType.STRING, Utils.getArmorSlots());
    }

    @Override
    public NamespacedKey getKey() {return key;}

    @Override
    public Random getRandom() {
        return new Random();
    }
}
