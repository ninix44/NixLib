package ru.ninix.nixlib.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.util.NixRenderUtils;

public class FractalGuiScreen extends Screen {

    private double targetX = -0.75;
    private double targetY = 0.0;
    private double currentX = -0.75;
    private double currentY = 0.0;

    private double targetZoom = 3.0;
    private double currentZoom = 3.0;

    private boolean isDragging = false;
    private double lastMouseX, lastMouseY;

    private int oldBlurValue;
    private boolean isInitialized = false;

    public FractalGuiScreen() {
        super(Component.literal("Mandelbrot Explorer"));
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        double zoomFactor = 0.85;

        if (scrollY > 0) {
            zoomTowards(mouseX, mouseY, zoomFactor);
        } else if (scrollY < 0) {
            zoomTowards(mouseX, mouseY, 1.0 / zoomFactor);
        }
        return true;
    }

    private void zoomTowards(double mouseX, double mouseY, double factor) {
        double aspectRatio = (double) this.width / this.height;

        double u = (mouseX / this.width - 0.5) * aspectRatio;

        double v = -(mouseY / this.height - 0.5);

        double worldMouseX = targetX + u * targetZoom;
        double worldMouseY = targetY + v * targetZoom;

        targetZoom *= factor;

        targetX = worldMouseX - u * targetZoom;
        targetY = worldMouseY - v * targetZoom;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isDragging) {
            double dx = mouseX - lastMouseX;
            double dy = mouseY - lastMouseY;

            double aspectRatio = (double) this.width / this.height;

            targetX -= (dx / this.width) * targetZoom * aspectRatio;

            targetY += (dy / this.height) * targetZoom;

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        double lerpSpeed = 0.2;
        currentX += (targetX - currentX) * lerpSpeed;
        currentY += (targetY - currentY) * lerpSpeed;
        currentZoom += (targetZoom - currentZoom) * lerpSpeed;

        ShaderInstance shader = NixLibShaders.getFractalShader();
        if (shader == null) return;

        int w = this.width;
        int h = this.height;

        NixRenderUtils.drawTexturedQuad(
            guiGraphics.pose().last().pose(),
            0, 0, w, h,
            shader,
            (s) -> {
                if (s.getUniform("uResolution") != null) s.getUniform("uResolution").set((float) w, (float) h);
                if (s.getUniform("uOffset") != null) s.getUniform("uOffset").set((float) currentX, (float) currentY);
                if (s.getUniform("uZoom") != null) s.getUniform("uZoom").set((float) currentZoom);
            }
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
