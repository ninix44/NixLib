package ru.ninix.nixlib.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import ru.ninix.nixlib.client.shader.NixLibShaders;

import java.util.Random;

public class ConstellationGameScreen extends Screen {

    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private float time = 0.0f;

    private int cardX, cardY;
    private final int cardWidth = 160;
    private final int cardHeight = 220;

    private final float[] starCoords = new float[10];
    private int connectedCount = 0;

    private final Random random = new Random();
    private boolean isWon = false;

    private int oldBlurValue;
    private boolean isInitialized = false;

    public ConstellationGameScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();

        if (!this.isInitialized) {
            this.oldBlurValue = this.minecraft.options.menuBackgroundBlurriness().get();
            generateStars();
            this.isInitialized = true;
        }

        this.minecraft.options.menuBackgroundBlurriness().set(0);
    }

    private void generateStars() {
        for (int i = 0; i < 5; i++) {
            boolean valid;
            float sx, sy;
            int attempts = 0;
            do {
                valid = true;
                sx = 0.15f + random.nextFloat() * 0.7f;
                sy = 0.15f + random.nextFloat() * 0.7f;

                for (int j = 0; j < i; j++) {
                    float px = starCoords[j * 2];
                    float py = starCoords[j * 2 + 1];
                    float dist = Mth.sqrt((sx - px) * (sx - px) + (sy - py) * (sy - py));
                    if (dist < 0.15f) {
                        valid = false;
                        break;
                    }
                }
                attempts++;
            } while (!valid && attempts < 50);

            starCoords[i * 2] = sx;
            starCoords[i * 2 + 1] = sy;
        }
        connectedCount = 0;
        isWon = false;
    }

    @Override
    public void onClose() {
        this.minecraft.options.menuBackgroundBlurriness().set(this.oldBlurValue);
        super.onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !isWon) {
            float relX = (float) (mouseX - cardX) / cardWidth;
            float relY = (float) (mouseY - cardY) / cardHeight;

            if (connectedCount < 5) {
                float tx = starCoords[connectedCount * 2];
                float ty = starCoords[connectedCount * 2 + 1];

                float dist = Mth.sqrt((relX - tx) * (relX - tx) + (relY - ty) * (relY - ty));

                if (dist < 0.08f) {
                    connectedCount++;

                    Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f + (connectedCount * 0.2f));

                    if (connectedCount >= 5) {
                        isWon = true;
                        Minecraft.getInstance().player.playSound(SoundEvents.PLAYER_LEVELUP, 0.5f, 1.0f);
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        this.cardX = (this.width - cardWidth) / 2;
        this.cardY = (this.height - cardHeight) / 2;

        time += partialTick * 0.01f;

        renderCard(cardX, cardY, cardWidth, cardHeight, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(int x, int y, int w, int h, int mouseX, int mouseY) {
        ShaderInstance shader = NixLibShaders.getConstellationCardShader();
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
        RenderSystem.setShader(() -> shader);

        if (shader.getUniform("uTime") != null) shader.getUniform("uTime").set(time);

        float relX = (float) (mouseX - x) / w;
        float relY = (float) (mouseY - y) / h;
        boolean isHovering = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        if (shader.getUniform("MousePos") != null) shader.getUniform("MousePos").set(relX, relY);
        if (shader.getUniform("HasFocus") != null) shader.getUniform("HasFocus").set(isHovering ? 1.0f : 0.0f);

        if (shader.getUniform("StarCoords") != null) {
            shader.getUniform("StarCoords").set(starCoords);
        }

        if (shader.getUniform("GameProgress") != null) {
            shader.getUniform("GameProgress").set((float) connectedCount);
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float z = 0.0f;
        bufferBuilder.addVertex(x, y + h, z).setUv(0, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x + w, y + h, z).setUv(1, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x + w, y, z).setUv(1, 0).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x, y, z).setUv(0, 0).setColor(255, 255, 255, 255);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
