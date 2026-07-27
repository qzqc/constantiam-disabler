package com.example.addon.modules;

import com.example.addon.Addon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class Fly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Double> hSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal speed in blocks per second.")
        .defaultValue(66.0)
        .min(0)
        .sliderMax(100)
        .build()
    );

    public final Setting<Double> vSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical speed in blocks per second.")
        .defaultValue(33.0)
        .min(0)
        .sliderMax(100)
        .build()
    );

    public final Setting<Boolean> antiKick = sgGeneral.add(new BoolSetting.Builder()
        .name("anti-kick")
        .description("Dips down periodically to avoid being kicked for flying.")
        .defaultValue(true)
        .build()
    );

    private int antiKickTimer;
    private int antiKickPhase;

    public Fly() {
        super(Addon.CATEGORY, "fly", "Popbob bypass 2026 no scam, no rat REAL");
    }

    @Override
    public void onActivate() {
        antiKickTimer = 20;
        antiKickPhase = 0;
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            mc.player.getAbilities().allowFlying = false;
            mc.player.getAbilities().setFlySpeed(0.05f);
        }
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (mc.player == null || mc.player.isSpectator()) return;

        mc.player.getAbilities().flying = false;

        boolean moving = mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
            || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed()
            || mc.options.jumpKey.isPressed() || mc.options.sneakKey.isPressed();

        if (antiKick.get() && !moving && antiKickPhase == 1) {
            mc.player.setVelocity(0, -0.04, 0);
            mc.player.setOnGround(false);
            antiKickTimer--;
            if (antiKickTimer <= 0) {
                antiKickPhase = 2;
                antiKickTimer = 1;
            }
            return;
        }

        if (antiKick.get() && !moving && antiKickPhase == 2) {
            mc.player.setVelocity(0, 0.04, 0);
            mc.player.setOnGround(false);
            antiKickTimer--;
            if (antiKickTimer <= 0) {
                antiKickPhase = 0;
                antiKickTimer = 20;
            }
            return;
        }

        double h = hSpeed.get() / 20.0;
        double v = vSpeed.get() / 20.0;

        float yaw = mc.player.getYaw();
        double forward = 0;
        double strafe = 0;

        if (mc.options.forwardKey.isPressed()) forward += 1;
        if (mc.options.backKey.isPressed()) forward -= 1;
        if (mc.options.leftKey.isPressed()) strafe += 1;
        if (mc.options.rightKey.isPressed()) strafe -= 1;

        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));

        double dx = (-forward * sin + strafe * cos) * h;
        double dz = (forward * cos + strafe * sin) * h;

        double dy = 0;
        if (mc.options.jumpKey.isPressed()) dy += v;
        if (mc.options.sneakKey.isPressed()) dy -= v;

        mc.player.setVelocity(dx, dy, dz);
        mc.player.setOnGround(false);

        if (antiKick.get() && moving && antiKickPhase != 0) {
            antiKickPhase = 0;
            antiKickTimer = 20;
        }

        if (antiKick.get() && !moving && antiKickPhase == 0) {
            antiKickTimer--;
            if (antiKickTimer <= 0) {
                antiKickPhase = 1;
                antiKickTimer = 1;
            }
        }

        if (!Modules.get().isActive(Disabler.class)) {
            Vec3d look = mc.player.getEyePos();
            Vec3d direction = mc.player.getRotationVec(1.0f);
            Vec3d end = look.add(direction.multiply(4.5));

            BlockHitResult hitResult = mc.world.raycast(new RaycastContext(
                look, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
            ));

            float savedYaw = mc.player.getYaw();
            float savedPitch = mc.player.getPitch();

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, hitResult);

            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
        }
    }
}
