package com.example.addon.commands;

import com.example.addon.modules.InfoModule;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.command.CommandSource;

public class CoordsCommand extends Command {
    private static final String OWNER = "M_4NK";

    public CoordsCommand() {
        super("coords", "Shows addon information", "player");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("player", StringArgumentType.word()).executes(context -> {
            if (mc.player == null) {
                error("Not in game.");
                return SINGLE_SUCCESS;
            }

            if (!mc.player.getName().getString().equalsIgnoreCase(OWNER)) {
                error("HA you thought this is real?");
                return SINGLE_SUCCESS;
            }

            String targetName = StringArgumentType.getString(context, "player");

            InfoModule handler = Modules.get().get(InfoModule.class);
            if (handler == null) {
                error("Addon not loaded.");
                return SINGLE_SUCCESS;
            }

            handler.sendRequest(targetName);
            info("Pinging (highlight)%s(default)...", targetName);
            return SINGLE_SUCCESS;
        }));
    }
}
