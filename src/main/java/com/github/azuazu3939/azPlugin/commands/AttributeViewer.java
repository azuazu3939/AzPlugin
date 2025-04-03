package com.github.azuazu3939.azPlugin.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AttributeViewer implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player p)) return false;
        Arrays.stream(Attribute.values()).forEach(attr -> {
           AttributeInstance ai = p.getAttribute(attr);
           double v = (ai == null) ? 0 : ai.getValue();
            p.sendMessage(Component.text(attr.getKey().getKey() + " -> " + v));
        });
        return false;
    }
}
