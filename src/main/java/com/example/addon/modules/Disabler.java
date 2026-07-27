package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Disabler extends Module {
    public Disabler() {
        super(Addon.CATEGORY, "disabler", "Popbob bypass 2026 no scam, no rat REAL");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.currentScreen != null) return;

        Vec3d look = mc.player.getEyePos();
        Vec3d direction = mc.player.getRotationVec(1.0f);
        Vec3d end = look.add(direction.multiply(4.5));

        BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
            look, end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult);
    }
}
