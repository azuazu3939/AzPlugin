package com.github.azuazu3939.azPlugin.lib;

import com.github.azuazu3939.azPlugin.AzPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DamageColor {

    @NotNull
    public static Map<String, String> getColors() {
        Plugin plugin = AzPlugin.getInstance();
        Map<String, String> map = new HashMap<>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("Colors");
        if (cs == null) return map;
        for (String key : cs.getKeys(false)) {
            map.put(key, plugin.getConfig().getString("Colors." + key));
        }
        return map;
    }
}
