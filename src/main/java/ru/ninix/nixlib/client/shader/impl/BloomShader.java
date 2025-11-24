package ru.ninix.nixlib.client.shader.impl;

import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.client.shader.RenderStage;
import ru.ninix.nixlib.client.shader.ShaderBase;
import ru.ninix.nixlib.client.shader.api.GlslUniform;

public class BloomShader extends ShaderBase {

    @GlslUniform("Intensity")
    private float intensity;

    @GlslUniform("Threshold")
    private float threshold;

    @GlslUniform("Radius")
    private float radius;

    public BloomShader(float intensity, float threshold, float radius) {
        super(ResourceLocation.fromNamespaceAndPath("nixlib", "shaders/post/bloom.json"), RenderStage.WORLD);
        this.intensity = intensity;
        this.threshold = threshold;
        this.radius = radius;
        this.setDurationTicks(-1);
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
