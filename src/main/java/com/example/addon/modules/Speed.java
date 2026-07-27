package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Speed extends Module {
    public Speed() {
        super(Addon.CATEGORY, "speed", "Popbob bypass 2026 no scam, no rat REAL");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        double speed = 66.0 / 20.0;

        double forward = 0;
        double strafe = 0;

        if (mc.options.forwardKey.isPressed()) forward += 1;
        if (mc.options.backKey.isPressed()) forward -= 1;
        if (mc.options.leftKey.isPressed()) strafe += 1;
        if (mc.options.rightKey.isPressed()) strafe -= 1;

        if (forward == 0 && strafe == 0) {
            mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
        } else {
            float yaw = mc.player.getYaw();
            double sin = Math.sin(Math.toRadians(yaw));
            double cos = Math.cos(Math.toRadians(yaw));

            double dx = (-forward * sin + strafe * cos) * speed;
            double dz = (forward * cos + strafe * sin) * speed;

            mc.player.setVelocity(dx, mc.player.getVelocity().y, dz);
        }

        if (Modules.get().isActive(Disabler.class)) return;

        Vec3d look = mc.player.getEyePos();
        Vec3d direction = mc.player.getRotationVec(1.0f);
        Vec3d end = look.add(direction.multiply(4.5));

        BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
            look, end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            mc.player
        ));

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult);

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
