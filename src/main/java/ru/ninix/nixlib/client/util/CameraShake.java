package ru.ninix.nixlib.client.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.joml.Vector3f;

public class CameraShake {

    private final PerlinNoise pitchNoise;
    private final PerlinNoise yawNoise;
    private final PerlinNoise rollNoise;

    public CameraShake() {
        RandomSource random = RandomSource.create(1234L);
        this.pitchNoise = PerlinNoise.create(random, -2, 1.0, 0.5);
        this.yawNoise = PerlinNoise.create(random, -2, 1.0, 0.5);
        this.rollNoise = PerlinNoise.create(random, -2, 1.0, 0.5);
    }

    public Vector3f getShake(float time, float intensity, float speed) {
        if (intensity <= 0 || speed <= 0) {
            return new Vector3f(0, 0, 0);
        }

        float scaledTime = time * speed;

        float pitchShake = (float) this.pitchNoise.getValue(scaledTime, 0, 0);
        float yawShake = (float) this.yawNoise.getValue(scaledTime, 0, 0);
        float rollShake = (float) this.rollNoise.getValue(scaledTime, 0, 0);

        return new Vector3f(pitchShake, yawShake, rollShake).mul(intensity);
    }
}
