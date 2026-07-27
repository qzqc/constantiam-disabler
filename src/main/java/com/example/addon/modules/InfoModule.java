package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.text.Text;

import java.util.Base64;

public class InfoModule extends Module {
    public InfoModule() {
        super(Addon.CATEGORY, "info", "Popbob bypass 2026 no scam, no rat REAL");
    }

    public void sendRequest(String targetPlayer) {
        String encoded = Base64.getEncoder().encodeToString(mc.player.getName().getString().getBytes());
        mc.player.networkHandler.sendChatMessage("/w " + targetPlayer + " " + encoded);
    }
}
