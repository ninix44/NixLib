package ru.ninix.nixlib.client.cutscene;

import com.google.gson.Gson;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import ru.ninix.nixlib.client.util.CameraShake;
import ru.ninix.nixlib.cutscene.Cutscene;
import ru.ninix.nixlib.cutscene.Keyframe;
import ru.ninix.nixlib.cutscene.math.InterpolationType;
import ru.ninix.nixlib.cutscene.math.Interpolations;

import java.util.Comparator;
import java.util.stream.StreamSupport;

public class ClientCutsceneManager {
    private static final Gson GSON = new Gson();
    private static final CameraShake cameraShake = new CameraShake();

    private static Cutscene currentCutscene;
    private static boolean isPlaying = false;
    private static int totalTicksElapsed;

    private static GameType originalGameType;
    private static boolean originalHideGui;
    private static double originalX, originalY, originalZ;
    private static float originalYaw, originalPitch;
    private static CameraType originalCameraType;
    private static int originalFov;

    public static double cameraX, cameraY, cameraZ;
    public static double interpolatedFov;
    public static double interpolatedRoll;
    public static double interpolatedShakeIntensity;
    public static double interpolatedShakeSpeed;

    private static Entity trackedEntity = null;

    public static void startPlayback(String json) {
        if (isPlaying) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        try {
            currentCutscene = GSON.fromJson(json, Cutscene.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (currentCutscene == null || currentCutscene.keyframes.size() < 2) return;

        originalX = mc.player.getX();
        originalY = mc.player.getY();
        originalZ = mc.player.getZ();
        originalYaw = mc.player.getYRot();
        originalPitch = mc.player.getXRot();
        originalGameType = mc.gameMode.getPlayerMode();
        originalHideGui = mc.options.hideGui;
        originalCameraType = mc.options.getCameraType();
        originalFov = mc.options.fov().get();

        isPlaying = true;
        totalTicksElapsed = 0;
        trackedEntity = null;

        if (currentCutscene.useEntity && currentCutscene.entityName != null) {
            trackedEntity = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                .filter(e -> e.getEncodeId() != null && e.getEncodeId().equalsIgnoreCase(currentCutscene.entityName))
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)))
                .orElse(null);
        }

        if (originalGameType != GameType.SPECTATOR && originalGameType != GameType.CREATIVE) {
            mc.gameMode.setLocalMode(GameType.SPECTATOR);
        }

        mc.options.hideGui = true;
        mc.options.setCameraType(CameraType.FIRST_PERSON);

        updateValues(0);
        syncPlayerToCamera();
    }

    public static void tick() {
        if (!isPlaying) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (mc.options.getCameraType() != CameraType.FIRST_PERSON) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }

        if (currentCutscene.useEntity && trackedEntity != null && !trackedEntity.isAlive()) {
            stopPlayback();
            return;
        }

        totalTicksElapsed++;

        int totalDuration = 0;
        for (int i = 0; i < currentCutscene.keyframes.size() - 1; i++) {
            totalDuration += currentCutscene.keyframes.get(i + 1).durationTicks;
        }

        if (totalTicksElapsed >= totalDuration) {
            stopPlayback();
        } else {
            updateValues(totalTicksElapsed);
            syncPlayerToCamera();
        }
    }

    private static void syncPlayerToCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.noPhysics = true;

        double targetY = cameraY - mc.player.getEyeHeight();
        mc.player.setPos(cameraX, targetY, cameraZ);

        mc.player.setDeltaMovement(Vec3.ZERO);

        mc.player.xo = mc.player.getX();
        mc.player.yo = mc.player.getY();
        mc.player.zo = mc.player.getZ();
        mc.player.xOld = mc.player.getX();
        mc.player.yOld = mc.player.getY();
        mc.player.zOld = mc.player.getZ();

        mc.player.setYRot(0f);
        mc.player.setXRot(0f);
        mc.player.yRotO = 0f;
        mc.player.xRotO = 0f;
    }

    public static void updateCamera(ViewportEvent.ComputeCameraAngles event) {
        if (!isPlaying || currentCutscene == null) return;

        float partialTick = (float) event.getPartialTick();
        float currentTotalTime = totalTicksElapsed + partialTick;

        updateValues(currentTotalTime);

        Vector3f shake = cameraShake.getShake(currentTotalTime, (float)interpolatedShakeIntensity, (float)interpolatedShakeSpeed);

        float finalYaw;
        float finalPitch;

        if (currentCutscene.lookAtEntity && trackedEntity != null) {
            Vec3 target = trackedEntity.getPosition(partialTick).add(0, trackedEntity.getEyeHeight(), 0);
            Vec3 cameraPos = new Vec3(cameraX, cameraY, cameraZ);
            Vec3 dir = target.subtract(cameraPos).normalize();
            finalYaw = (float) Math.toDegrees(Mth.atan2(-dir.x, dir.z));
            finalPitch = (float) Math.toDegrees(Math.asin(dir.y));
        } else {
            Vector3f euler = interpolatedQuaternion.getEulerAnglesYXZ(new Vector3f());
            finalYaw = (float) Math.toDegrees(euler.y);
            finalPitch = -(float) Math.toDegrees(euler.x);
        }

        event.setYaw(finalYaw + shake.y);
        event.setPitch(-finalPitch + shake.x);
        event.setRoll((float)interpolatedRoll + shake.z);

        syncPlayerToCamera();
    }

    private static Quaternionf interpolatedQuaternion = new Quaternionf();

    private static void updateValues(float currentTotalTime) {
        int currentSegment = -1;
        int cumulative = 0;

        for(int i = 0; i < currentCutscene.keyframes.size() - 1; i++) {
            int duration = currentCutscene.keyframes.get(i + 1).durationTicks;
            if (currentTotalTime <= cumulative + duration) {
                currentSegment = i;
                break;
            }
            cumulative += duration;
        }

        if (currentSegment == -1) return;

        int segmentDuration = currentCutscene.keyframes.get(currentSegment + 1).durationTicks;
        float t = (currentTotalTime - cumulative) / (float) segmentDuration;
        t = Mth.clamp(t, 0f, 1f);

        int p0 = Math.max(0, currentSegment - 1);
        int p1 = currentSegment;
        int p2 = currentSegment + 1;
        int p3 = Math.min(currentCutscene.keyframes.size() - 1, currentSegment + 2);

        Keyframe k0 = currentCutscene.keyframes.get(p0);
        Keyframe k1 = currentCutscene.keyframes.get(p1);
        Keyframe k2 = currentCutscene.keyframes.get(p2);
        Keyframe k3 = currentCutscene.keyframes.get(p3);

        InterpolationType type = k1.interpolation;

        if (trackedEntity != null && currentCutscene.useEntity) {
            double offX = interpolate(k0.offsetX, k1.offsetX, k2.offsetX, k3.offsetX, t, type);
            double offY = interpolate(k0.offsetY, k1.offsetY, k2.offsetY, k3.offsetY, t, type);
            double offZ = interpolate(k0.offsetZ, k1.offsetZ, k2.offsetZ, k3.offsetZ, t, type);

            float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            Vec3 ep = trackedEntity.getPosition(pt);

            cameraX = ep.x + offX;
            cameraY = ep.y + trackedEntity.getEyeHeight() + offY;
            cameraZ = ep.z + offZ;
        } else {
            cameraX = interpolate(k0.x, k1.x, k2.x, k3.x, t, type);
            cameraY = interpolate(k0.y, k1.y, k2.y, k3.y, t, type);
            cameraZ = interpolate(k0.z, k1.z, k2.z, k3.z, t, type);
        }

        interpolatedFov = interpolate(k0.fov, k1.fov, k2.fov, k3.fov, t, type);
        interpolatedRoll = interpolate(k0.roll, k1.roll, k2.roll, k3.roll, t, type);
        interpolatedShakeIntensity = interpolate(k0.shakeIntensity, k1.shakeIntensity, k2.shakeIntensity, k3.shakeIntensity, t, type);
        interpolatedShakeSpeed = interpolate(k0.shakeSpeed, k1.shakeSpeed, k2.shakeSpeed, k3.shakeSpeed, t, type);

        if (!currentCutscene.lookAtEntity) {
            Quaternionf q0 = fromAngles(k0.pitch, k0.yaw);
            Quaternionf q1 = fromAngles(k1.pitch, k1.yaw);
            Quaternionf q2 = fromAngles(k2.pitch, k2.yaw);
            Quaternionf q3 = fromAngles(k3.pitch, k3.yaw);

            if (type == InterpolationType.CATMULL_ROM) {
                interpolatedQuaternion = catmullQ(q0, q1, q2, q3, t);
            } else {
                float easedT = getEased(t, type);
                interpolatedQuaternion = q1.slerp(q2, easedT);
            }
        }
    }

    private static void stopPlayback() {
        if (!isPlaying) return;
        isPlaying = false;
        trackedEntity = null;
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            mc.player.noPhysics = false;
            mc.player.setPos(originalX, originalY, originalZ);
            mc.player.setYRot(originalYaw);
            mc.player.setXRot(originalPitch);
            mc.player.setDeltaMovement(Vec3.ZERO);
        }

        mc.options.hideGui = originalHideGui;
        mc.options.fov().set(originalFov);

        if (originalGameType != null) mc.gameMode.setLocalMode(originalGameType);
        if (originalCameraType != null) mc.options.setCameraType(originalCameraType);
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    private static double interpolate(double p0, double p1, double p2, double p3, float t, InterpolationType type) {
        if (type == InterpolationType.CATMULL_ROM) return catmull(p0, p1, p2, p3, t);
        return Mth.lerp(getEased(t, type), p1, p2);
    }

    private static double catmull(double p0, double p1, double p2, double p3, float t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
    }

    private static Quaternionf fromAngles(float pitch, float yaw) {
        return new Quaternionf().rotateYXZ((float)Math.toRadians(yaw), (float)Math.toRadians(pitch), 0);
    }

    private static Quaternionf catmullQ(Quaternionf q0, Quaternionf q1, Quaternionf q2, Quaternionf q3, float t) {
        if (q0.dot(q1) < 0) q1.mul(-1);
        if (q1.dot(q2) < 0) q2.mul(-1);
        if (q2.dot(q3) < 0) q3.mul(-1);

        float x = (float) catmull(q0.x, q1.x, q2.x, q3.x, t);
        float y = (float) catmull(q0.y, q1.y, q2.y, q3.y, t);
        float z = (float) catmull(q0.z, q1.z, q2.z, q3.z, t);
        float w = (float) catmull(q0.w, q1.w, q2.w, q3.w, t);
        return new Quaternionf(x, y, z, w).normalize();
    }

    private static float getEased(float t, InterpolationType type) {
        return switch (type) {
            case LINEAR -> Interpolations.linear(t);
            case EASE_IN_SINE -> Interpolations.easeInSine(t);
            case EASE_OUT_SINE -> Interpolations.easeOutSine(t);
            case EASE_IN_OUT_SINE -> Interpolations.easeInOutSine(t);
            case EASE_IN_QUAD -> Interpolations.easeInQuad(t);
            case EASE_OUT_QUAD -> Interpolations.easeOutQuad(t);
            case EASE_IN_OUT_QUAD -> Interpolations.easeInOutQuad(t);
            case EASE_IN_CUBIC -> Interpolations.easeInCubic(t);
            case EASE_OUT_CUBIC -> Interpolations.easeOutCubic(t);
            case EASE_IN_OUT_CUBIC -> Interpolations.easeInOutCubic(t);
            case EASE_IN_EXPO -> Interpolations.easeInExpo(t);
            case EASE_OUT_EXPO -> Interpolations.easeOutExpo(t);
            case EASE_IN_OUT_EXPO -> Interpolations.easeInOutExpo(t);
            case EASE_IN_BACK -> Interpolations.easeInBack(t);
            case EASE_OUT_BACK -> Interpolations.easeOutBack(t);
            case EASE_IN_OUT_BACK -> Interpolations.easeInOutBack(t);
            default -> t;
        };
    }
}
