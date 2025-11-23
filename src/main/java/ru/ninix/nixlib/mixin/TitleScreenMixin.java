package ru.ninix.nixlib.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ninix.nixlib.client.shader.NixLibShaders;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Shadow
    @Nullable
    private SplashRenderer splash;

    @Unique
    private float cosmicTime = 0.0f;

    @Unique
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    @Inject(method = "init", at = @At("TAIL"))
    private void setCustomSplash(CallbackInfo ci) {
        this.splash = new SplashRenderer("Thank you SSKirillSS :> "); // thank you really SSKirillSS
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void renderCosmicBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        cosmicTime += partialTick * 0.01f;

        ShaderInstance shader = NixLibShaders.getCosmicShader();

        if (shader != null) {
            int guiWidth = guiGraphics.guiWidth();
            int guiHeight = guiGraphics.guiHeight();
            float physicalWidth = Minecraft.getInstance().getWindow().getWidth();
            float physicalHeight = Minecraft.getInstance().getWindow().getHeight();

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
            RenderSystem.setShader(() -> shader);

            if (shader.getUniform("uTime") != null) {
                shader.getUniform("uTime").set(cosmicTime);
            }

            if (shader.getUniform("ScreenSize") != null) {
                shader.getUniform("ScreenSize").set(physicalWidth, physicalHeight);
            }

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

            bufferBuilder.addVertex(0, guiHeight, 0).setUv(0, 1).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(guiWidth, guiHeight, 0).setUv(1, 1).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(guiWidth, 0, 0).setUv(1, 0).setColor(255, 255, 255, 255);
            bufferBuilder.addVertex(0, 0, 0).setUv(0, 0).setColor(255, 255, 255, 255);

            BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

            RenderSystem.disableBlend();
        }
    }

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void cancelPanorama(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        ci.cancel();
    }
}
