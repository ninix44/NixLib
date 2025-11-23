package ru.ninix.nixlib.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.ninix.nixlib.NixLib;
import ru.ninix.nixlib.client.shader.NixLibShaders;
import ru.ninix.nixlib.client.shader.ShaderAPI;

public class CosmicGuiScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NixLib.MODID, "textures/gui/cosmic_gui.png");
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private final int imageWidth = 256;
    private final int imageHeight = 256;

    private final int cosmicW = 128;
    private final int cosmicH = 128;
    private final int cosmicX = 64;
    private final int cosmicY = 64;

    private float time = 0.0f;
    private int oldBlurValue;

    public CosmicGuiScreen() {
        super(Component.literal("Cosmic GUI"));
    }

    @Override
    protected void init() {
        super.init();
        ResourceLocation blackHole = ResourceLocation.fromNamespaceAndPath("nixlib", "shaders/post/black_hole.json");
        if (ShaderAPI.isLoaded(blackHole)) {
            ShaderAPI.unload(blackHole);
        }
        this.oldBlurValue = this.minecraft.options.menuBackgroundBlurriness().get();
        this.minecraft.options.menuBackgroundBlurriness().set(0);
    }

    @Override
    public void onClose() {
        this.minecraft.options.menuBackgroundBlurriness().set(this.oldBlurValue);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        guiGraphics.fill(leftPos + cosmicX, topPos + cosmicY, leftPos + cosmicX + cosmicW, topPos + cosmicY + cosmicH, 0xFF000000);

        renderCosmicPortal(leftPos + cosmicX, topPos + cosmicY, cosmicW, cosmicH, mouseX, mouseY, partialTick);

        int textWidth = this.font.width(this.title);
        int textX = (this.width - textWidth) / 2;
        guiGraphics.drawString(this.font, this.title, textX, topPos - 12, 0xFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderCosmicPortal(int x, int y, int w, int h, int mouseX, int mouseY, float partialTick) {
        time += partialTick * 0.01f;

        ShaderInstance shader = NixLibShaders.getCosmicShader();
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
        RenderSystem.setShader(() -> shader);

        if (shader.getUniform("uTime") != null) shader.getUniform("uTime").set(time);

        if (shader.getUniform("MousePos") != null) {
            float relX = (float) (mouseX - x) / w;
            float relY = (float) (mouseY - y) / h;

            if (mouseX < x - 50 || mouseX > x + w + 50 || mouseY < y - 50 || mouseY > y + h + 50) {
                shader.getUniform("MousePos").set(-10.0f, -10.0f);
            } else {
                shader.getUniform("MousePos").set(relX, relY);
            }
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float zLevel = 0.01f;

        bufferBuilder.addVertex(x, y + h, zLevel).setUv(0, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x + w, y + h, zLevel).setUv(1, 1).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x + w, y, zLevel).setUv(1, 0).setColor(255, 255, 255, 255);
        bufferBuilder.addVertex(x, y, zLevel).setUv(0, 0).setColor(255, 255, 255, 255);

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }
}
