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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ShaderBase {
    protected final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation shaderLocation;
    private final RenderStage renderStage;

    private int durationTicks = -1;
    private int ticks = 0;

    @Nullable
    private PostChain postChain;

    private final Map<String, Field> cachedUniforms = new HashMap<>();

    private final List<Consumer<EffectInstance>> uniformAppliers = new ArrayList<>();

    protected ShaderBase(ResourceLocation shaderLocation, RenderStage renderStage) {
        this.shaderLocation = shaderLocation;
        this.renderStage = renderStage;

        scanForUniforms();

        this.addUniformApplier(this::applyReflectedUniforms);
    }

    private void scanForUniforms() {
        Class<?> clazz = this.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(GlslUniform.class)) {
                    field.setAccessible(true);
                    String uniformName = field.getAnnotation(GlslUniform.class).value();
                    cachedUniforms.put(uniformName, field);
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
            if (ticks >= durationTicks) {
                durationTicks = 0;
            }
        }
    }

    public boolean isFinished() {
        return durationTicks == 0;
    }

    public void createPostChain() {
        if (postChain != null) {
            postChain.close();
        }
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

        for (var pass : postChain.passes) {
            EffectInstance effectInstance = pass.getEffect();
            if (effectInstance != null) {
                applyUniforms(effectInstance);
            }
        }

        postChain.process(partialTicks);
    }

    public void resize(int width, int height) {
        if (postChain != null) {
            postChain.resize(width, height);
        }
    }

    public void close() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
    }

    public void addUniformApplier(Consumer<EffectInstance> applier) {
        if (applier != null) uniformAppliers.add(applier);
    }

    private void applyUniforms(EffectInstance effectInstance) {
        for (Consumer<EffectInstance> c : uniformAppliers) {
            try {
                c.accept(effectInstance);
            } catch (Throwable t) {
                NixLib.LOGGER.error("Failed to apply uniform for shader: {}", shaderLocation, t);
            }
        }
    }

    private void applyReflectedUniforms(EffectInstance effect) {
        cachedUniforms.forEach((name, field) -> {
            Uniform uniform = effect.getUniform(name);
            if (uniform != null) {
                try {
                    Class<?> type = field.getType();

                    if (type == float.class) {
                        uniform.set(field.getFloat(this));
                    } else if (type == int.class) {
                        uniform.set(field.getInt(this));
                    } else if (type == boolean.class) {
                        uniform.set(field.getBoolean(this) ? 1.0f : 0.0f);
                    } else if (type == float[].class) {
                        uniform.set((float[]) field.get(this));
                    }

                } catch (IllegalAccessException e) {
                    NixLib.LOGGER.error("Failed to access reflected field: " + field.getName(), e);
                }
            }
        });
    }

    public ResourceLocation getShaderLocation() { return shaderLocation; }
    public RenderStage getRenderStage() { return renderStage; }
    public void setDurationTicks(int ticks) { this.durationTicks = ticks; }
    @Nullable public PostChain getPostChain() { return postChain; }
}
