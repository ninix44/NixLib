package ru.ninix.nixlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.util.NixRenderUtils;

public class FractalGuiScreen extends Screen {

    private float time = 0.0f;
    private int oldBlurValue;
    private boolean isInitialized = false;

    public FractalGuiScreen() {
        super(Component.literal("Fractal Math"));
    }

    @Override
    protected void init() {
        super.init();
        if (!this.isInitialized) {
            this.oldBlurValue = this.minecraft.options.menuBackgroundBlurriness().get();
            this.isInitialized = true;
        }
        this.minecraft.options.menuBackgroundBlurriness().set(0);
    }

    @Override
    public void onClose() {
        this.minecraft.options.menuBackgroundBlurriness().set(this.oldBlurValue);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        time += partialTick * 0.005f;

        ShaderInstance shader = NixLibShaders.getFractalShader();
        if (shader == null) return;

        int w = this.width;
        int h = this.height;

        NixRenderUtils.drawTexturedQuad(
            guiGraphics.pose().last().pose(),
            0, 0, w, h,
            shader,
            (s) -> {
                if (s.getUniform("uTime") != null) s.getUniform("uTime").set(time);
                if (s.getUniform("uResolution") != null) s.getUniform("uResolution").set((float) w, (float) h);
                if (s.getUniform("uMouse") != null) s.getUniform("uMouse").set((float) mouseX, (float) mouseY);
            }
        );

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
