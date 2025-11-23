package ru.ninix.nixlib.client.shader.impl;

import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.client.shader.InfiniteShader;
import ru.ninix.nixlib.client.shader.RenderStage;

public class BlackHoleShader extends InfiniteShader {

    private final float strength;
    private final float swirl;

    public BlackHoleShader(float strength, float swirl) {
        super(ResourceLocation.fromNamespaceAndPath("nixlib", "shaders/post/black_hole.json"), RenderStage.WORLD);
        this.strength = strength;
        this.swirl = swirl;

        this.addUniformApplier(effect -> {
            if (effect == null) return;

            var strengthU = effect.getUniform("Strength");
            if (strengthU != null) strengthU.set(this.strength);

            var swirlU = effect.getUniform("Swirl");
            if (swirlU != null) swirlU.set(this.swirl);
        });
    }
}
