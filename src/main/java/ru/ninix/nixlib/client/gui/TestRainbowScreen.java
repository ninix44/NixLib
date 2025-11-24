package ru.ninix.nixlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.util.NixRenderUtils;

public class TestRainbowScreen extends Screen {

    private float time;

    private int oldBlurValue;

    public TestRainbowScreen() {
        super(Component.literal("Shader Geometry Test"));
    }

    @Override
    protected void init() {
        super.init();
        // disable menu blur to see the custom geometry clearly
        this.oldBlurValue = this.minecraft.options.menuBackgroundBlurriness().get();
        this.minecraft.options.menuBackgroundBlurriness().set(0);
    }

    @Override
    public void onClose() {
        // restore blur settings
        this.minecraft.options.menuBackgroundBlurriness().set(this.oldBlurValue);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        time += partialTick * 0.05f;

        ShaderInstance shader = NixLibShaders.getRainbowShader();
        if (shader != null) {
            float cx = width / 2.0f;
            float cy = height / 2.0f;
            float size = 100.0f;

            NixRenderUtils.drawCustomGeometry(
                shader,
                // uniform setup
                (s) -> { if(s.getUniform("uTime") != null) s.getUniform("uTime").set(time); },

                // geometric construction (triangle, or another geometric figure)
                (buffer) -> {
                    Matrix4f m = guiGraphics.pose().last().pose();
                    // Top
                    buffer.addVertex(m, cx, cy - size, 0).setUv(0.5f, 0.0f);
                    // bottom right
                    buffer.addVertex(m, cx + size, cy + size, 0).setUv(1.0f, 1.0f);
                    // bottom left (repeated to close "QUAD")
                    buffer.addVertex(m, cx - size, cy + size, 0).setUv(0.0f, 1.0f);
                    buffer.addVertex(m, cx - size, cy + size, 0).setUv(0.0f, 1.0f);
                }
            );
        }

        //guiGraphics.drawCenteredString(font, "Custom Geometry Test", width/2, height/2 + 120, 0xFFFFFF); testikkkkkk (test gui text)
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
