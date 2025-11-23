package ru.ninix.nixlib.client.shader;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.NixLib;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
public abstract class ShaderBase {
    protected final Minecraft mc = Minecraft.getInstance();
    private final ResourceLocation shaderLocation;
    private final RenderStage renderStage;

    private int durationTicks = -1;
    private int ticks = 0;

    @Nullable
    private PostChain postChain;

    private final List<Consumer<EffectInstance>> uniformAppliers = new ArrayList<>();

    protected ShaderBase(ResourceLocation shaderLocation, RenderStage renderStage) {
        this.shaderLocation = shaderLocation;
        this.renderStage = renderStage;
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

    public ResourceLocation getShaderLocation() { return shaderLocation; }
    public RenderStage getRenderStage() { return renderStage; }
    public void setDurationTicks(int ticks) { this.durationTicks = ticks; }
    @Nullable public PostChain getPostChain() { return postChain; }
}
