package ru.ninix.nixlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.util.NixRenderUtils;

public class ChladniGameScreen extends Screen {

    private float time = 0.0f;

    private int cardX, cardY;
    private final int cardSize = 256;

    private int oldBlurValue;
    private boolean isInitialized = false;

    private float targetFreqX = 1.0f;
    private float targetFreqY = 1.0f;
    private float currentFreqX = 1.0f;
    private float currentFreqY = 1.0f;

    public ChladniGameScreen() {
        super(Component.literal("Chladni Resonance"));
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

        this.cardX = (this.width - cardSize) / 2;
        this.cardY = (this.height - cardSize) / 2;

        time += partialTick * 0.01f;

        boolean isHovering = mouseX >= cardX && mouseX <= cardX + cardSize && mouseY >= cardY && mouseY <= cardY + cardSize;

        if (isHovering) {
            float relX = (float) (mouseX - cardX) / cardSize;
            float relY = (float) (mouseY - cardY) / cardSize;
            targetFreqX = relX;
            targetFreqY = relY;
        }

        float lerpSpeed = 0.08f;
        currentFreqX = currentFreqX + (targetFreqX - currentFreqX) * lerpSpeed;
        currentFreqY = currentFreqY + (targetFreqY - currentFreqY) * lerpSpeed;

        renderPlate(guiGraphics);

        float valX = 1.0f + currentFreqX * 20.0f;
        float valY = 1.0f + currentFreqY * 20.0f;

        boolean snapX = Math.abs(valX - Math.round(valX)) < 0.1;
        boolean snapY = Math.abs(valY - Math.round(valY)) < 0.1;
        int colorX = snapX ? 0x55FF55 : 0xAAAAAA;
        int colorY = snapY ? 0x55FF55 : 0xAAAAAA;



        if(snapX && snapY) {
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPlate(GuiGraphics guiGraphics) {
        ShaderInstance shader = NixLibShaders.getChladniPlateShader();
        if (shader == null) return;

        NixRenderUtils.drawTexturedQuad(
            guiGraphics.pose().last().pose(),
            cardX, cardY, cardSize, cardSize,
            shader,
            (s) -> {
                safeSet(s, "uTime", time);
                safeSet(s, "MousePos", currentFreqX, currentFreqY);
                safeSet(s, "Resolution", 1.0f);
            }
        );
    }

    private void safeSet(ShaderInstance s, String name, float... vals) {
        if (s.getUniform(name) != null) s.getUniform(name).set(vals);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
