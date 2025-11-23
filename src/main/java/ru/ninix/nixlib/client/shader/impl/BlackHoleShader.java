package ru.ninix.nixlib.client.shader.impl;

import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.client.shader.InfiniteShader;
import ru.ninix.nixlib.client.shader.RenderStage;
import ru.ninix.nixlib.client.shader.api.GlslUniform;

public class BlackHoleShader extends InfiniteShader {

    @GlslUniform("Strength")
    private float strength;

    @GlslUniform("Swirl")
    private float swirl;

    public BlackHoleShader(float strength, float swirl) {
        super(ResourceLocation.fromNamespaceAndPath("nixlib", "shaders/post/black_hole.json"), RenderStage.WORLD);
        this.strength = strength;
        this.swirl = swirl;

    }

    public void setStrength(float strength) {
        this.strength = strength;
    }
}
