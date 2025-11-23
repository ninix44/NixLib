package ru.ninix.nixlib.client.shader;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class InfiniteShader extends ShaderBase {

    protected final long startTime;

    protected InfiniteShader(ResourceLocation shaderLocation, RenderStage renderStage) {
        super(shaderLocation, renderStage);
        this.startTime = System.currentTimeMillis();

        this.setDurationTicks(-1);

        this.addUniformApplier(this::applyTime);
    }

    private void applyTime(EffectInstance effect) {
        if (effect == null) return;

        float timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0f;

        var timeU = effect.getUniform("uTime");
        if (timeU != null) {
            timeU.set(timeSeconds);
        }
    }
}
