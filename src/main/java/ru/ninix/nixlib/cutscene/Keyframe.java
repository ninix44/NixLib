package ru.ninix.nixlib.cutscene;

import ru.ninix.nixlib.cutscene.math.InterpolationType;

public class Keyframe {
    public double x, y, z;
    public float pitch, yaw, roll;
    public float fov;
    public int durationTicks;
    public InterpolationType interpolation = InterpolationType.CATMULL_ROM;
    public float shakeIntensity = 0.0f;
    public float shakeSpeed = 0.0f;
    public double offsetX = 0.0, offsetY = 0.0, offsetZ = 0.0;

    public Keyframe() {
    }

    public Keyframe(double x, double y, double z, float pitch, float yaw, float roll, float fov, int durationTicks, float shakeIntensity, float shakeSpeed) {
        this.x = x; this.y = y; this.z = z;
        this.pitch = pitch; this.yaw = yaw; this.roll = roll;
        this.fov = fov;
        this.durationTicks = durationTicks;
        this.shakeIntensity = shakeIntensity;
        this.shakeSpeed = shakeSpeed;
    }
}
