package ru.ninix.nixlib.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import ru.ninix.nixlib.client.util.NixRenderUtils;

import java.util.function.Supplier;

public class ShaderBlockRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final Supplier<ShaderInstance> shaderSupplier;
    private final Settings settings;

    // IMPORTANT!!! DON't CHANGE the default values here (true/false) !!!
    // CHANGE them in the "NixLib.java -> registerRenderers"  file when you register the block!!!

    public static class Settings {
        public boolean renderSolidBlock = true; // draw a cube - yes
        public boolean renderFloorGlow = false; // draw the floor - no
        public boolean renderCenterGlow = false; // draw a ball inside - no
        public float floorRadius = 3.0f; // floor radius
        public float centerRadius = 2.0f; // center radius

        public Settings solid(boolean enabled) { this.renderSolidBlock = enabled; return this; }
        public Settings floor(float radius) { this.renderFloorGlow = true; this.floorRadius = radius; return this; }
        public Settings noFloor() { this.renderFloorGlow = false; return this; }
        public Settings center(float radius) { this.renderCenterGlow = true; this.centerRadius = radius; return this; }
        public Settings noCenter() { this.renderCenterGlow = false; return this; }
    }

    public ShaderBlockRenderer(BlockEntityRendererProvider.Context context, Supplier<ShaderInstance> shaderSupplier, Settings settings) {
        this.shaderSupplier = shaderSupplier;
        this.settings = settings;
    }

    @Override
    public void render(T entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ShaderInstance shader = shaderSupplier.get();
        if (shader == null) return;

        float time = (entity.getLevel().getGameTime() + partialTick) * 0.05f;

        poseStack.pushPose();

        if (settings.renderSolidBlock) {
            float scale = 1.002f;
            float offset = (1.0f - scale) / 2.0f;
            poseStack.translate(offset, offset, offset);
            poseStack.scale(scale, scale, scale);

            NixRenderUtils.drawCubeWithShader(
                poseStack.last().pose(),
                shader,
                false,
                (s) -> {
                    if (s.getUniform("uTime") != null) s.getUniform("uTime").set(time);
                    if (s.getUniform("uType") != null) s.getUniform("uType").set(0.0f);
                }
            );
            poseStack.popPose();
            poseStack.pushPose();
        }

        if (settings.renderFloorGlow) {
            NixRenderUtils.drawLightPlane(
                poseStack.last().pose(),
                shader,
                0.5f, 0.05f, 0.5f,
                settings.floorRadius,
                (s) -> {
                    if (s.getUniform("uTime") != null) s.getUniform("uTime").set(time);
                    if (s.getUniform("uType") != null) s.getUniform("uType").set(2.0f);
                }
            );
        }

        if (settings.renderCenterGlow) {
            NixRenderUtils.draw3DLightCross(
                poseStack.last().pose(),
                shader,
                0.5f, 0.5f, 0.5f,
                settings.centerRadius,
                (s) -> {
                    if (s.getUniform("uTime") != null) s.getUniform("uTime").set(time);
                    if (s.getUniform("uType") != null) s.getUniform("uType").set(1.0f);
                }
            );
        }

        poseStack.popPose();
    }
}
