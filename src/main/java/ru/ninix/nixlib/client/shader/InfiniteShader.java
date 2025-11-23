package ru.ninix.nixlib.client.shader;

import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.client.shader.api.GlslUniform;

public abstract class InfiniteShader extends ShaderBase {

    protected final long startTime;

    @GlslUniform("uTime")
    protected float timeSeconds;

    protected InfiniteShader(ResourceLocation shaderLocation, RenderStage renderStage) {
        super(shaderLocation, renderStage);
        this.startTime = System.currentTimeMillis();
        this.setDurationTicks(-1);
    }

    @Override
    public void process(float partialTicks) {
        this.timeSeconds = (System.currentTimeMillis() - startTime) / 1000.0f;

        super.process(partialTicks);
    }
}
