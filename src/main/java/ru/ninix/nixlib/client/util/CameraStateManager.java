package ru.ninix.nixlib.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import ru.ninix.nixlib.NixLib;

public class CameraStateManager {
    private static boolean isEditorActive = false;
    private static float cameraFov = 80.0f;
    private static float cameraRoll = 0.0f;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            isEditorActive = false;
            return;
        }
        boolean holdingCamera = mc.player.isHolding(NixLib.CAMERA_ITEM.get());
        if (isEditorActive && !holdingCamera) {
            isEditorActive = false;
        }
    }

    public static void activate() {
        if (!isEditorActive) {
            isEditorActive = true;
            cameraFov = Minecraft.getInstance().options.fov().get().floatValue();
            cameraRoll = 0;
        }
    }

    public static void updateFov(double delta) {
        activate();
        cameraFov -= (float) (delta * 2.5f);
        cameraFov = Mth.clamp(cameraFov, 1.0f, 170.0f);
    }

    public static void adjustRoll(float delta) {
        activate();
        cameraRoll += delta;
        cameraRoll = Mth.wrapDegrees(cameraRoll);
    }

    public static void resetRoll() {
        activate(); cameraRoll = 0f;
    }

    public static boolean isActive() {
        return isEditorActive;
    }

    public static float getFov() {
        return cameraFov;
    }

    public static float getRoll() {
        return cameraRoll;
    }
}
