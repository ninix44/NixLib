package ru.ninix.nixlib.client.shader;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.NixLib;
import ru.ninix.nixlib.client.shader.api.GlslUniform;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class ShaderBase {
    protected final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation shaderLocation;
    private final RenderStage renderStage;

    private int durationTicks = -1;
    private int ticks = 0;
    protected final long startTime;

    @Nullable
    private PostChain postChain;

    private final Map<String, Field> cachedUniforms = new HashMap<>();

    protected ShaderBase(ResourceLocation shaderLocation, RenderStage renderStage) {
        this.shaderLocation = shaderLocation;
        this.renderStage = renderStage;
        this.startTime = System.currentTimeMillis();
        scanForUniforms();
    }

    private void scanForUniforms() {
        Class<?> clazz = this.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GlslUniform.class)) {
                    field.setAccessible(true);
                    cachedUniforms.put(field.getAnnotation(GlslUniform.class).value(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public void onEnable() {
        createPostChain();
    }

    public void onDisable() {
        close();
    }

    public void onTick() {
        if (durationTicks > 0) {
            ticks++;
        }
    }

    public boolean isFinished() {
        return durationTicks > 0 && ticks >= durationTicks;
    }

    public void createPostChain() {
        if (postChain != null) postChain.close();
        try {
            postChain = new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), shaderLocation);
            postChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        } catch (IOException | JsonSyntaxException e) {
            NixLib.LOGGER.error("Failed to load shader: {}", shaderLocation, e);
            postChain = null;
        }
    }

    public void process(float partialTicks) {
        if (postChain == null) return;

        float timeSec = (System.currentTimeMillis() - startTime) / 1000.0f;

        for (var pass : postChain.passes) {
            EffectInstance effect = pass.getEffect();
            if (effect != null) {
                Uniform uTime = effect.getUniform("uTime");
                if (uTime != null) uTime.set(timeSec);

                applyReflectedUniforms(effect);
            }
        }

        postChain.process(partialTicks);
    }

    private void applyReflectedUniforms(EffectInstance effect) {
        cachedUniforms.forEach((name, field) -> {
            Uniform uniform = effect.getUniform(name);
            if (uniform != null) {
                try {
                    Class<?> type = field.getType();
                    if (type == float.class) uniform.set(field.getFloat(this));
                    else if (type == int.class) uniform.set(field.getInt(this));
                    else if (type == boolean.class) uniform.set(field.getBoolean(this) ? 1.0f : 0.0f);
                    else if (type == float[].class) uniform.set((float[]) field.get(this));
                } catch (IllegalAccessException e) {
                    NixLib.LOGGER.error("Failed access field: " + field.getName(), e);
                }
            }
        });
    }

    public void resize(int width, int height) {
        if (postChain != null) postChain.resize(width, height);
    }

    public void close() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
    }

    public ResourceLocation getShaderLocation() { return shaderLocation; }
    public RenderStage getRenderStage() { return renderStage; }
    public void setDurationTicks(int ticks) { this.durationTicks = ticks; }
    @Nullable public PostChain getPostChain() { return postChain; }
}
